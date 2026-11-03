package com.example.orders.domain;

/**
 * 注文の1明細（商品・単価・数量）を表すイミュータブルなクラスです。
 */
public class OrderLine {
    private final String productId;
    private final int unitPrice;
    private final int quantity;

    /**
     * 明細を生成します。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public OrderLine(String productId, int unitPrice, int quantity) {
        this.productId = productId;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String productId() { return productId; }
    public int unitPrice() { return unitPrice; }
    public int quantity() { return quantity; }
}
