package com.example.orders.ch12_event_driven.exercise.query;


/**
 * 参照側が返す注文の概要です（読み取り専用）。
 * テストがこの形を前提にしているため、形は変えないでください。
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
