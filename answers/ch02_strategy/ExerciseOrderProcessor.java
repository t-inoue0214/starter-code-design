package com.example.orders.ch02_strategy.exercise;

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
 * 第2章の解答例です。割引の if-else を Strategy に分解しています。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    private final PrintStream out;
    private final List<DiscountStrategy> strategies;

    /**
     * 標準の割引ルール一式で注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 標準の割引ルール一式を渡す（組み立て役はここ）
        this(stock, out, List.of(new MemberRankDiscount(), new CouponDiscount(), new SeasonalDiscount()));
    }

    /**
     * 割引ルールを外から受け取るコンストラクタです（コンストラクタ注入）。
     *
     * @param stock      商品IDごとの在庫数
     * @param out        通知・決済・監査ログの出力先
     * @param strategies 適用する割引ルール
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out, List<DiscountStrategy> strategies) {
        this.stock = stock;
        this.out = out;
        this.strategies = List.copyOf(strategies);
    }

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
        // 割引額は、ルールごとの割引額の合計（どんなルールがあるかは知らない）
        int discount = 0;
        for (DiscountStrategy strategy : strategies) {
            discount += strategy.discountFor(order, subtotal);
        }
        if (discount > subtotal) {
            discount = subtotal;
        }
        int total = subtotal - discount;

        // 3. 在庫引当（全明細の在庫を確認してから引き当てる）
        for (OrderLine line : order.lines()) {
            int available = stock.getOrDefault(line.productId(), 0);
            if (available < line.quantity()) {
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
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知
        if (order.customer().email() != null) {
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + total);
        }
        if (order.customer().phone() != null && total >= 10000) {
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total);

        return new OrderResult(order.id(), subtotal, discount, total);
    }
}
