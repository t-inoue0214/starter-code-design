package com.example.orders.ch16_template.exercise;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.PrintStream;
import java.util.Map;

/**
 * 注文処理の骨格（手順）を定義する抽象クラスです。
 * 手順（検証、金額計算、在庫、決済、通知、監査）は固定し、チャネルごとに違う部分だけを子クラスに任せます。
 */
public abstract class AbstractOrderProcessor implements OrderProcessor {
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    /** 子クラスが出力に使う出力先 */
    protected final PrintStream out;

    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    protected AbstractOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 決済を受け付ける上限額です。既定は50万円で、必要なら子クラスが上書きします（フックメソッド）。
     *
     * @return 上限額（円）
     */
    protected int paymentLimit() {
        // 標準の決済上限（50万円）を返す
        return 500000;
    }

    /**
     * 注文成立後に、顧客へ通知します。チャネルごとに必ず決める部分です（抽象メソッド）。
     *
     * @param order  注文
     * @param result 金額の内訳
     */
    protected abstract void notifyCustomer(Order order, OrderResult result);

    /**
     * 監査ログの末尾に付ける文字列です。既定は付けません（フックメソッド）。
     *
     * @return 末尾に付ける文字列
     */
    protected String auditSuffix() {
        // 標準では、監査ログに何も付け足さない
        return "";
    }

    /**
     * 注文処理の手順を固定します。手順は子クラスから上書きできません。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public final OrderResult process(Order order) {
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
        // 請求額を、小計から割引額を引いて求める
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

        // 4. 決済（上限額を超える注文は拒否。拒否されたら在庫を戻す）
        if (total > paymentLimit()) {
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

        // ★重要★ 5. 通知（チャネルごとの部分）
        OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
        // ★重要★ 通知の中身は、子クラスが決める（ここは呼ぶだけ）
        notifyCustomer(order, result);

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total
                + auditSuffix());

        // 処理の結果を、そのまま返す
        return result;
    }
}
