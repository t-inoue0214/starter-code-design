package com.example.orders.ch07_event_driven.exercise.query;


/**
 * 参照側が返す注文の概要です（読み取り専用）。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public class OrderSummary {
    // 注文IDを持つフィールド
    private final String orderId;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 請求額（円）を持つフィールド
    private final int total;

    /**
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public OrderSummary(String orderId, String customerId, int total) {
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の請求額（円）を、フィールドに保存する
        this.total = total;
    }

    // 注文IDを返す
    public String orderId() { return orderId; }
    // 顧客IDを返す
    public String customerId() { return customerId; }
    // 請求額（円）を返す
    public int total() { return total; }
}
