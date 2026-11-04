package com.example.orders.ch08_composition.exercise;

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
 * 第8章の解答例です。継承をやめ、決済のあとの処理を {@link OrderFollowUp} の部品として持ちます（コンポジション）。
 * 処理の組み合わせは、使う側が {@code List} で決めます。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 出力先を持つフィールド
    private final PrintStream out;
    // 決済のあとの処理の一覧を持つフィールド
    private final List<OrderFollowUp> followUps;

    /**
     * 追加の処理を持たない、土台だけの処理を作ります。
     *
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 追加の処理なしで、もう一方のコンストラクタを呼ぶ
        this(stock, out, List.of());
    }

    /**
     * 決済のあとに行う追加の処理を、部品として受け取ります。
     *
     * @param stock     商品IDごとの在庫数
     * @param out       出力先
     * @param followUps 決済のあとに、順に実行する追加の処理
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out, List<OrderFollowUp> followUps) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
        // 引数の決済のあとの処理の一覧を、フィールドに保存する
        this.followUps = followUps;
    }

    /**
     * 通知と監査ログを含む、標準の注文処理を作ります。契約テストは、このメソッドで作った処理に当てます。
     * 部品の組み合わせは、ここで決めます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     * @return 標準の注文処理
     */
    public static OrderProcessor standard(Map<String, Integer> stock, PrintStream out) {
        // ★重要★ どの部品を、どの順で使うかは、組み立てる側が決める
        return new ExerciseOrderProcessor(stock, out,
                List.of(new MailFollowUp(out), new SmsFollowUp(out), new AuditFollowUp(out)));
    }

    /**
     * 検証、金額計算、在庫引当、決済を行います。
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
            // 在庫が、注文の数量に足りないとき
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
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

        // 決済までの結果を作る（あとの処理に渡す）
        OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
        // ★重要★ 5. 決済のあとの処理（通知・監査ログなど）は、持っている部品に任せる（委譲）
        for (OrderFollowUp followUp : followUps) {
            // ★重要★ 追加の処理の中身は知らず、部品に任せる（委譲）
            followUp.run(order, result);
        }
        // 処理の結果を、そのまま返す
        return result;
    }
}
