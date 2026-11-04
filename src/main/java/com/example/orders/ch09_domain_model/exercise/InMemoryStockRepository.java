package com.example.orders.ch09_domain_model.exercise;

import java.util.Map;

/**
 * Map を保存先にした StockRepository です。
 * TODO: find は Map の値から Stock を作り（なければ在庫0）、save は Map に書き戻してください。
 */
public class InMemoryStockRepository implements StockRepository {
    /**
     * @param stocks 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public InMemoryStockRepository(Map<String, Integer> stocks) {
    }

    /**
     * 商品の在庫を取り出します。
     *
     * @param productId 商品ID
     * @return 在庫
     */
    @Override
    public Stock find(String productId) {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }

    /**
     * 在庫を保存します。
     *
     * @param stock 商品IDごとの在庫数
     */
    @Override
    public void save(Stock stock) {
        throw new UnsupportedOperationException("TODO: 第9章の課題です");
    }
}
