package com.example.orders.ch09_domain_model.exercise;

import com.example.orders.domain.OutOfStockException;

/**
 * 商品の在庫を表すエンティティです。「在庫が足りるか」というルールを、自分自身が持ちます。
 */
public class Stock {
    private final String productId;
    private int quantity;

    /**
     * @param productId 商品ID
     * @param quantity  在庫数
     */
    public Stock(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    /** @return 商品ID */
    public String productId() {
        return productId;
    }

    /** @return 現在の在庫数 */
    public int quantity() {
        return quantity;
    }

    /**
     * 在庫を引き当てます。足りなければ例外です。
     *
     * @param amount 数量
     */
    public void reserve(int amount) {
        // ★重要★ 「足りるか」のルールは、在庫自身が持つ
        if (amount > quantity) {
            throw new OutOfStockException("在庫が不足しています: " + productId);
        }
        quantity -= amount;
    }

    /**
     * 引き当てた在庫を戻します。
     *
     * @param amount 数量
     */
    public void release(int amount) {
        quantity += amount;
    }
}
