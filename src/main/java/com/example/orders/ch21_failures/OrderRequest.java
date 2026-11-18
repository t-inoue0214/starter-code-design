package com.example.orders.ch21_failures;

/**
 * 画面やAPIから届いた、注文の入力です。届いたままの文字列を運ぶ DTO で、検証はしません。
 */
public final class OrderRequest {
    // 顧客IDを持つフィールド（空のこともある）
    private final String customerId;
    // 商品IDを持つフィールド（空のこともある）
    private final String productId;
    // 数量を、届いたままの文字列で持つフィールド
    private final String quantityText;

    /**
     * 入力を作ります。
     *
     * @param customerId   顧客ID
     * @param productId    商品ID
     * @param quantityText 数量（文字列のまま）
     */
    public OrderRequest(String customerId, String productId, String quantityText) {
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の数量の文字列を、フィールドに保存する
        this.quantityText = quantityText;
    }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 商品ID */
    public String productId() { return productId; }

    /** @return 数量（文字列のまま） */
    public String quantityText() { return quantityText; }
}
