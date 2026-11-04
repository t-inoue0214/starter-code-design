package com.example.orders.domain;

/**
 * 注文処理の結果（金額の内訳）を表すイミュータブルなクラスです。
 */
public class OrderResult {
    // 注文IDを持つフィールド
    private final String orderId;
    // 小計（円）を持つフィールド
    private final int subtotal;
    // 割引額（円）を持つフィールド
    private final int discount;
    // 請求額（円）を持つフィールド
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
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数の小計（円）を、フィールドに保存する
        this.subtotal = subtotal;
        // 引数の割引額（円）を、フィールドに保存する
        this.discount = discount;
        // 引数の請求額（円）を、フィールドに保存する
        this.total = total;
    }

    // 注文IDを返す
    public String orderId() { return orderId; }
    // 小計（円）を返す
    public int subtotal() { return subtotal; }
    // 割引額（円）を返す
    public int discount() { return discount; }
    // 請求額（円）を返す
    public int total() { return total; }
}
