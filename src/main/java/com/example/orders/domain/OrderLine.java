package com.example.orders.domain;

/**
 * 注文の1明細（商品・単価・数量）を表すイミュータブルなクラスです。
 */
public class OrderLine {
    // 商品IDを持つフィールド
    private final String productId;
    // 単価（円）を持つフィールド
    private final int unitPrice;
    // 数量を持つフィールド
    private final int quantity;

    /**
     * 明細を生成します。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public OrderLine(String productId, int unitPrice, int quantity) {
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の単価（円）を、フィールドに保存する
        this.unitPrice = unitPrice;
        // 引数の数量を、フィールドに保存する
        this.quantity = quantity;
    }

    // 商品IDを返す
    public String productId() { return productId; }
    // 単価（円）を返す
    public int unitPrice() { return unitPrice; }
    // 数量を返す
    public int quantity() { return quantity; }
}
