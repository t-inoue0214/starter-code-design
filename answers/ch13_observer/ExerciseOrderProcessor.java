package com.example.orders.ch13_observer.exercise;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * 第13章の解答例です。通知と監査ログを Observer（リスナー）に分解しています。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 出力先を持つフィールド
    private final PrintStream out;
    // 通知先の一覧を持つフィールド
    private final List<OrderPlacedListener> listeners;

    /**
     * 標準の通知先一式で注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // ★重要★ 標準の通知先を渡す（組み立て役はここ）
        this(stock, out, List.of(new MailNotifier(out), new SmsNotifier(out), new AuditLogger(out)));
    }

    /**
     * 通知先（リスナー）を外から受け取るコンストラクタです。
     *
     * @param stock     商品IDごとの在庫数
     * @param out       決済ログの出力先
     * @param listeners 注文成立後に呼ばれる通知先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out, List<OrderPlacedListener> listeners) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
        // 通知先を外から受け取り、書き換えられないコピーとして保存する
        this.listeners = List.copyOf(listeners);
    }

    /**
     * 注文を処理します。成立したあとの通知は、登録された通知先に任せます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // 1. 入力検証
        if (order == null || order.customer() == null) {
            // 注文か注文者がないことを、検証エラーとして投げる
            throw new OrderValidationException("注文または注文者がありません");
        }
        // 明細がない注文は、処理できない
        if (order.lines() == null || order.lines().isEmpty()) {
            // 明細がないことを、検証エラーとして投げる
            throw new OrderValidationException("明細がありません");
        }
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 数量が0以下の明細は、不正として扱う
            if (line.quantity() <= 0) {
                // 数量が不正であることを、検証エラーとして投げる
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            // 単価が負の明細は、不正として扱う
            if (line.unitPrice() < 0) {
                // 単価が負であることを、検証エラーとして投げる
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }

        // 2. 金額計算
        int subtotal = 0;
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 単価 × 数量を、小計に加える
            subtotal += line.unitPrice() * line.quantity();
        }
        // 割引額を0から数え始める
        int discount = 0;
        // ゴールド会員なら
        if (order.customer().rank() == MemberRank.GOLD) {
            // 小計の10%を、割引額に加える
            discount += subtotal * 10 / 100;
        // シルバー会員なら
        } else if (order.customer().rank() == MemberRank.SILVER) {
            // 小計の5%を、割引額に加える
            discount += subtotal * 5 / 100;
        }
        // 注文に指定されたクーポンコードを取り出す
        String coupon = order.couponCode();
        // クーポンが指定されているときだけ
        if (coupon != null) {
            // WELCOME500 は、500円引きのクーポン
            if (coupon.equals("WELCOME500")) {
                // 500円を、割引額に加える
                discount += 500;
            // SAVE10 は、小計の10%引きのクーポン
            } else if (coupon.equals("SAVE10")) {
                // 小計の10%を、割引額に加える
                discount += subtotal * 10 / 100;
            // それ以外のとき
            } else {
                // 登録されていないクーポンは、検証エラーとして投げる
                throw new OrderValidationException("未知のクーポンです: " + coupon);
            }
        }
        // 注文日が12月なら
        if (order.orderedOn().getMonthValue() == 12) {
            // 小計の3%を、季節割引として加える
            discount += subtotal * 3 / 100;
        }
        // 割引額が小計を超えたとき
        if (discount > subtotal) {
            // 割引額を小計までに抑える
            discount = subtotal;
        }
        // 請求額 = 小計 − 割引
        int total = subtotal - discount;

        // 3. 在庫引当（全明細の在庫を確認してから引き当てる）
        for (OrderLine line : order.lines()) {
            // 商品の現在の在庫数を調べる（登録がなければ0）
            int available = stock.getOrDefault(line.productId(), 0);
            // 在庫が注文の数量に足りないとき
            if (available < line.quantity()) {
                // 在庫が足りないので、在庫不足の例外を投げる
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 商品の在庫を、注文の数量だけ減らす
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }

        // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
        if (total > 500000) {
            // 注文の明細を1行ずつ取り出して処理する
            for (OrderLine line : order.lines()) {
                // 商品の在庫を、注文の数量だけ戻す
                stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
            }
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // ★重要★ 5. 注文が成立したことを、登録されたすべての通知先に知らせる（誰がいるかは知らない）
        OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
        // 登録された通知先を、1つずつ取り出す
        for (OrderPlacedListener listener : listeners) {
            // ★重要★ 通知の中身は知らず、登録された通知先に知らせるだけ
            listener.onOrderPlaced(order, result);
        }

        // 処理の結果を、そのまま返す
        return result;
    }
}
