package com.example.orders.ch06_template.exercise;

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
 * 店頭注文用の実装です。最初は分解前のコピーです。
 * AbstractOrderProcessor を継承し、店頭固有の部分だけを残してください。
 */
public class StoreOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    private final PrintStream out;

    /**
     * 神クラスを生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public StoreOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        this.stock = stock;
        this.out = out;
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

        // 4. 決済（店頭は100万円を超える注文を拒否。拒否されたら在庫を戻す）
        if (total > 1000000) {
            for (OrderLine line : order.lines()) {
                stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
            }
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知（店頭では、その場で伝えるため通知しない）

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total
                + " channel=STORE");

        return new OrderResult(order.id(), subtotal, discount, total);
    }
}
