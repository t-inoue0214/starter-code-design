package com.example.orders.ch20_visibility.order;

/**
 * 注文の行です。外から読めるように public にしていますが、作れるのはこのパッケージの中だけです。
 * そのため、確認を通っていない行が、外で作られることはありません。
 */
public final class OrderLine {
    // 商品IDを持つフィールド
    private final String productId;
    // 単価（円）を持つフィールド
    private final int unitPrice;
    // 数量を持つフィールド
    private final int quantity;

    /**
     * 行を作ります。Order からだけ呼ばれます。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    OrderLine(String productId, int unitPrice, int quantity) {
        // ★重要★ 作る前に上限を確かめる。コンストラクタが package-private なので、この確認を迂回できない
        PriceRules.checkQuantity(quantity);
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の単価を、フィールドに保存する
        this.unitPrice = unitPrice;
        // 引数の数量を、フィールドに保存する
        this.quantity = quantity;
    }

    /**
     * 数量を足した、新しい行を返します。Order が同じ商品をまとめるときに使います。
     *
     * @param more 足す数量
     * @return 数量を足した、新しい行
     */
    OrderLine plus(int more) {
        // 足した数量で、新しい行を作って返す（上限の確認も通る）
        return new OrderLine(productId, unitPrice, quantity + more);
    }

    /** @return 小計（円） */
    public int subtotal() {
        // 計算は、パッケージの中の決まりに任せる
        return PriceRules.subtotal(unitPrice, quantity);
    }

    /** @return 商品ID */
    public String productId() { return productId; }

    /** @return 単価（円） */
    public int unitPrice() { return unitPrice; }

    /** @return 数量 */
    public int quantity() { return quantity; }
}
