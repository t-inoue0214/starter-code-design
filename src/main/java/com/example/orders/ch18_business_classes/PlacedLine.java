package com.example.orders.ch18_business_classes;

/**
 * 集約の中の部品（Value Object）です。注文の1明細を表し、変更できません。
 * 数量を変えたいときは、新しい明細を作り直します。
 */
public final class PlacedLine {
    // 商品IDを持つフィールド
    private final String productId;
    // 単価（円）を持つフィールド
    private final int unitPrice;
    // 数量を持つフィールド
    private final int quantity;

    /**
     * 明細を作ります。検証は、集約のルート（PlacedOrder）が済ませてから呼びます。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    PlacedLine(String productId, int unitPrice, int quantity) {
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の単価を、フィールドに保存する
        this.unitPrice = unitPrice;
        // 引数の数量を、フィールドに保存する
        this.quantity = quantity;
    }

    /**
     * 数量を足した新しい明細を作ります。元の明細は変わりません。
     *
     * @param extra 足す数量
     * @return 数量を足した新しい明細
     */
    PlacedLine plusQuantity(int extra) {
        // 同じ商品と単価で、数量だけを足した明細を新しく作る
        return new PlacedLine(productId, unitPrice, quantity + extra);
    }

    /** @return この明細の金額（単価 × 数量） */
    public int subtotal() {
        // 単価に数量を掛けた金額を返す
        return unitPrice * quantity;
    }

    /** @return 商品ID */
    public String productId() { return productId; }

    /** @return 単価（円） */
    public int unitPrice() { return unitPrice; }

    /** @return 数量 */
    public int quantity() { return quantity; }
}
