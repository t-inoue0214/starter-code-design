package com.example.orders.ch02_roles.exercise;

/**
 * 注文の概要を運ぶだけのクラスです（DTO）。ルールも手順も持たず、値を運びます。
 */
public class OrderSummaryDto {
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
    public OrderSummaryDto(String orderId, String customerId, int total) {
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
