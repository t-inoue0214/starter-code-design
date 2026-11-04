package com.example.orders.ch07_event_driven.exercise.event;


/**
 * 「注文が成立した」という事実を表すイベントです。過去に起きたことを表すため、不変です。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public class OrderPlacedEvent {
    private final String orderId;
    private final String customerId;
    private final int total;

    /**
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public OrderPlacedEvent(String orderId, String customerId, int total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
    }

    public String orderId() { return orderId; }
    public String customerId() { return customerId; }
    public int total() { return total; }
}
