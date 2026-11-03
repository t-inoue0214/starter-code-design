package com.example.orders.ch05_factory.exercise;

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
 * 第5章の解答例です。クーポンの選択を CouponRuleFactory に分けています。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    private final PrintStream out;
    private final CouponRuleFactory couponFactory;

    /**
     * 神クラスを生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 標準の工場を渡す（組み立て役はここ）
        this(stock, out, new CouponRuleFactory());
    }

    /**
     * クーポンの工場を外から受け取るコンストラクタです。
     *
     * @param stock   商品IDごとの在庫数
     * @param out     出力先
     * @param factory クーポンコードから割引ルールを作る工場
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out, CouponRuleFactory factory) {
        this.stock = stock;
        this.out = out;
        this.couponFactory = factory;
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
        int discount = 0;
        if (order.customer().rank() == MemberRank.GOLD) {
            discount += subtotal * 10 / 100;
        } else if (order.customer().rank() == MemberRank.SILVER) {
            discount += subtotal * 5 / 100;
        }
        // クーポンの種類は知らず、工場が作ったルールに任せる
        discount += couponFactory.create(order.couponCode()).discountFor(subtotal);
        if (order.orderedOn().getMonthValue() == 12) {
            discount += subtotal * 3 / 100;
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
