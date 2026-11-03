package com.example.orders.domain;

/**
 * 注文処理の結果（金額の内訳）を表すイミュータブルなクラスです。
 */
public class OrderResult {
    private final String orderId;
    private final int subtotal;
    private final int discount;
    private final int total;

    /**
     * 結果を生成します。
     *
     * @param orderId  注文ID
     * @param subtotal 割引前の小計（円）
     * @param discount 割引額の合計（円）
     * @param total    請求額（円）
     */
    public OrderResult(String orderId, int subtotal, int discount, int total) {
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
    }

    public String orderId() { return orderId; }
    public int subtotal() { return subtotal; }
    public int discount() { return discount; }
    public int total() { return total; }
}
