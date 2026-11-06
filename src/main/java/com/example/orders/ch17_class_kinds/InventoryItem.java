package com.example.orders.ch17_class_kinds;

import com.example.orders.domain.OutOfStockException;

/**
 * Entity（エンティティ）の例です。商品IDで区別され、在庫の数が変化します。
 * 「足りなければ引き当てられない」というルールを、自分で持ちます。
 */
public class InventoryItem {
    // 商品IDを持つフィールド
    private final String productId;
    // 数量を持つフィールド
    private int quantity;

    /**
     * 在庫を作ります。
     *
     * @param productId 商品ID（この在庫を区別する識別子）
     * @param quantity  現在の在庫数
     */
    public InventoryItem(String productId, int quantity) {
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
        // 引数の数量を、フィールドに保存する
        this.quantity = quantity;
    }

    /**
     * 在庫を引き当てます。足りなければ、在庫は変わりません。
     *
     * @param amount 引き当てる数
     */
    public void reserve(int amount) {
        // 引き当てる数が、在庫より多いとき
        if (amount > quantity) {
            // ★重要★ 「足りるか」のルールは、この Entity が自分で持つ
            throw new OutOfStockException("在庫が不足しています: " + productId);
        }
        // ★重要★ 在庫の数を減らす（Entity は、状態が変わる）
        quantity -= amount;
    }

    /** @return 商品ID */
    public String productId() { return productId; }

    /** @return 現在の在庫数 */
    public int quantity() { return quantity; }

    /**
     * 商品IDが同じなら、同じ在庫とみなします（数量が違っても同じ）。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // ★重要★ 商品IDが同じなら同じもの（Entity は ID で比べる）
        return o instanceof InventoryItem && ((InventoryItem) o).productId.equals(productId);
    }

    /** @return 商品IDから計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 商品IDから計算したハッシュ値を返す（equals と整合させる）
        return productId.hashCode();
    }
}
