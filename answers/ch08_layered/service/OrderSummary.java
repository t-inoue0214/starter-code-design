package com.example.orders.ch08_layered.exercise.service;


/**
 * 注文の概要です（業務層の型）。表示層には、この型で渡します。
 */
public class OrderSummary {
    private final String orderId;
    private final String customerId;
    private final int total;

    /**
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public OrderSummary(String orderId, String customerId, int total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
    }

    public String orderId() { return orderId; }
    public String customerId() { return customerId; }
    public int total() { return total; }
}
