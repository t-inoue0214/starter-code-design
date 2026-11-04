package com.example.orders.ch09_domain_model.exercise;

/**
 * 商品の在庫を表すエンティティです。「在庫が足りるか」というルールを、自分自身が持ちます。
 * TODO: 次の仕様で実装してください（テストがこの形を前提にしています）。
 *   - reserve: 数量が在庫を超えるなら OutOfStockException("在庫が不足しています: 商品ID")。足りれば減らす
 *   - release: 数量を在庫に戻す
 */
public class Stock {
    /**
     * @param productId 商品ID
     * @param quantity  在庫数
     */
    public Stock(String productId, int quantity) {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }

    /** @return 商品ID */
    public String productId() {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }

    /** @return 現在の在庫数 */
    public int quantity() {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }

    /**
     * 在庫を引き当てます。
     *
     * @param amount 数量
     */
    public void reserve(int amount) {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }

    /**
     * 引き当てた在庫を戻します。
     *
     * @param amount 数量
     */
    public void release(int amount) {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }
}
