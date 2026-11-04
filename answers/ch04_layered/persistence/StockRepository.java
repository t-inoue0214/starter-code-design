package com.example.orders.ch04_layered.exercise.persistence;

import java.util.Map;

/**
 * 在庫の保存先です（永続化層）。ここはデータの読み書きだけを行い、業務ルールは持ちません。
 */
public class StockRepository {
    private final Map<String, Integer> stocks;

    /**
     * @param stocks 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public StockRepository(Map<String, Integer> stocks) {
        this.stocks = stocks;
    }

    /**
     * @param productId 商品ID
     * @return 在庫数（存在しなければ0）
     */
    public int available(String productId) {
        return stocks.getOrDefault(productId, 0);
    }

    /**
     * @param productId 商品ID
     * @param delta     増減する数（減らすときは負）
     */
    public void change(String productId, int delta) {
        stocks.put(productId, available(productId) + delta);
    }
}
