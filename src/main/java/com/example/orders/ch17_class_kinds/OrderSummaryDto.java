package com.example.orders.ch17_class_kinds;

/**
 * DTO（Data Transfer Object）の例です。層や部品の間で、値を運ぶだけのクラスです。
 * ルールを持たず、値の検証もしません。
 */
public class OrderSummaryDto {
    // 注文IDを持つフィールド
    private final String orderId;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 請求額（円）を持つフィールド
    private final int total;

    /**
     * 値を、そのまま保存して運びます。
     *
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public OrderSummaryDto(String orderId, String customerId, int total) {
        // ★重要★ 受け取った値を、検証せずそのまま保存する（DTO はルールを持たない）
        this.orderId = orderId;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の請求額（円）を、フィールドに保存する
        this.total = total;
    }

    /** @return 注文ID */
    public String orderId() { return orderId; }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 請求額（円） */
    public int total() { return total; }
}
