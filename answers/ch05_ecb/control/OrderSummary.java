package com.example.orders.ch05_ecb.exercise.control;

/**
 * Control が Boundary に返す、注文の概要です（値を運ぶだけのクラス）。
 * Boundary は Entity を直接知らないので、必要な値だけをこの型に詰めて渡します。
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
