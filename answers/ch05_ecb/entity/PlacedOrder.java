package com.example.orders.ch05_ecb.exercise.entity;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.PaymentDeclinedException;

/**
 * 成立した注文という業務の概念です（Entity）。金額の計算と、決済上限のルールを持ちます。
 */
public class PlacedOrder {
    private static final int PAYMENT_LIMIT = 500000;

    private final String orderId;
    private final String customerId;
    private final int subtotal;
    private final int discount;

    /**
     * 成立した注文を作ります。外からは from で作ります。
     *
     * @param orderId 注文ID
     * @param customerId 顧客ID
     * @param subtotal 小計（円）
     * @param discount 割引額（円）
     */
    private PlacedOrder(String orderId, String customerId, int subtotal, int discount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.subtotal = subtotal;
        this.discount = discount;
    }

    /**
     * 注文から、成立した注文を作ります。金額を計算し、決済上限を超えていれば拒否します。
     *
     * @param order 注文
     * @return 成立した注文
     * @throws PaymentDeclinedException 請求額が決済上限を超えるとき
     */
    public static PlacedOrder from(Order order) {
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        PlacedOrder placed = new PlacedOrder(order.id(), order.customer().id(), subtotal,
                rankDiscount(order.customer().rank(), subtotal));
        if (placed.total() > PAYMENT_LIMIT) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        return placed;
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank 会員ランク
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    private static int rankDiscount(MemberRank rank, int subtotal) {
        if (rank == MemberRank.GOLD) {
            return subtotal * 10 / 100;
        }
        if (rank == MemberRank.SILVER) {
            return subtotal * 5 / 100;
        }
        return 0;
    }

    public String orderId() { return orderId; }
    public String customerId() { return customerId; }
    public int subtotal() { return subtotal; }
    public int discount() { return discount; }
    public int total() { return subtotal - discount; }
}
