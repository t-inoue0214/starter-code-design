package com.example.orders.ch19_immutability;

/**
 * イミュータブルなカートの1行です。技法: with メソッド（一部だけ変えた新しいインスタンスを返す）と、
 * イミュータブルな部品だけでできていること。
 */
public final class CartLine {
    // 商品コードを持つフィールド（イミュータブル）
    private final Sku sku;
    // 単価を持つフィールド（イミュータブル）
    private final Money unitPrice;
    // 数量を持つフィールド（イミュータブル）
    private final Quantity quantity;

    /**
     * カートの1行を作ります。
     *
     * @param sku       商品コード
     * @param unitPrice 単価
     * @param quantity  数量
     */
    public CartLine(Sku sku, Money unitPrice, Quantity quantity) {
        // 引数の商品コードを、フィールドに保存する
        this.sku = sku;
        // 引数の単価を、フィールドに保存する
        this.unitPrice = unitPrice;
        // 引数の数量を、フィールドに保存する
        this.quantity = quantity;
    }

    /**
     * 数量だけを変えた、新しい行を返します。この行自身は変わりません。
     *
     * @param newQuantity 新しい数量
     * @return 数量だけが違う、新しい行
     */
    public CartLine withQuantity(Quantity newQuantity) {
        // ★重要★ セッターの代わりに、変えたい項目だけが違う新しいインスタンスを返す
        return new CartLine(sku, unitPrice, newQuantity);
    }

    /** @return この行の金額（単価 × 数量） */
    public Money subtotal() {
        // 単価に数量を掛けた金額を返す
        return unitPrice.times(quantity);
    }

    /** @return 商品コード */
    public Sku sku() { return sku; }

    /** @return 単価 */
    public Money unitPrice() { return unitPrice; }

    /** @return 数量 */
    public Quantity quantity() { return quantity; }
}
