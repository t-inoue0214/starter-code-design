package com.example.orders.ch20_visibility.before;

/**
 * すべてを public にした注文の行です。コンストラクタもセッターも、誰でも呼べます。
 * 数量の上限の確認は OpenOrder の addItem にあるのに、この行のセッターからは確認なしで変えられます。
 */
public class OpenOrderLine {
    // 商品IDを持つフィールド
    private final String productId;
    // 単価（円）を持つフィールド
    private final int unitPrice;
    // 数量を持つフィールド（誰でも書き換えられる）
    private int quantity;

    /**
     * 行を作ります。誰でも、確認なしで作れます。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public OpenOrderLine(String productId, int unitPrice, int quantity) {
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の単価を、フィールドに保存する
        this.unitPrice = unitPrice;
        // 引数の数量を、確認せずにフィールドへ保存する
        this.quantity = quantity;
    }

    /**
     * 数量を書き換えます。上限の確認はしません。
     *
     * @param quantity 新しい数量
     */
    public void setQuantity(int quantity) {
        // 確認せずに、数量を書き換える
        this.quantity = quantity;
    }

    /** @return 商品ID */
    public String productId() { return productId; }

    /** @return 単価（円） */
    public int unitPrice() { return unitPrice; }

    /** @return 数量 */
    public int quantity() { return quantity; }
}
