package com.example.orders.ch02_roles.exercise;

/**
 * 注文の概要を運ぶだけのクラスです（DTO）。ルールも手順も持たず、値を運びます。
 */
public class OrderSummaryDto {
    private final String orderId;
    private final String customerId;
    private final int total;

    /**
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public OrderSummaryDto(String orderId, String customerId, int total) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.total = total;
    }

    public String orderId() { return orderId; }
    public String customerId() { return customerId; }
    public int total() { return total; }
}
