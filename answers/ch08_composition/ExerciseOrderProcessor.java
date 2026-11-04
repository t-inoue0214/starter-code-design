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
    private final Map<String, Integer> stock;
    private final PrintStream out;
    private final List<OrderFollowUp> followUps;

    /**
     * 追加の処理を持たない、土台だけの処理を作ります。
     *
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
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
        this.stock = stock;
        this.out = out;
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
            throw new OrderValidationException("注文または注文者がありません");
        }
        if (order.lines() == null || order.lines().isEmpty()) {
            throw new OrderValidationException("明細がありません");
        }
        for (OrderLine line : order.lines()) {
            if (line.quantity() <= 0) {
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            if (line.unitPrice() < 0) {
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }

        // 2. 金額計算
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        int discount = 0;
        if (order.customer().rank() == MemberRank.GOLD) {
            discount += subtotal * 10 / 100;
        } else if (order.customer().rank() == MemberRank.SILVER) {
            discount += subtotal * 5 / 100;
        }
        String coupon = order.couponCode();
        if (coupon != null) {
            if (coupon.equals("WELCOME500")) {
                discount += 500;
            } else if (coupon.equals("SAVE10")) {
                discount += subtotal * 10 / 100;
            } else {
                throw new OrderValidationException("未知のクーポンです: " + coupon);
            }
        }
        if (order.orderedOn().getMonthValue() == 12) {
            discount += subtotal * 3 / 100;
        }
        if (discount > subtotal) {
            discount = subtotal;
        }
        // 請求額 = 小計 − 割引
        int total = subtotal - discount;

        // 3. 在庫引当（全明細の在庫を確認してから引き当てる）
        for (OrderLine line : order.lines()) {
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }

        // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
        if (total > 500000) {
            for (OrderLine line : order.lines()) {
                stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
            }
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
        // ★重要★ 5. 決済のあとの処理（通知・監査ログなど）は、持っている部品に任せる（委譲）
        for (OrderFollowUp followUp : followUps) {
            followUp.run(order, result);
        }
        return result;
    }
}
