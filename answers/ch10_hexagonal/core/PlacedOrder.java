package com.example.orders.ch10_hexagonal.exercise.core;


/**
 * 成立した注文1件分の記録です（業務の中心の型）。
 */
public class PlacedOrder {
    private final String orderId;
    private final String customerId;
    private final int total;

    /**
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public PlacedOrder(String orderId, String customerId, int total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
    }

    public String orderId() { return orderId; }
    public String customerId() { return customerId; }
    public int total() { return total; }
}
