package com.example.orders.ch07_domain_model.exercise;

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

    @Override
    public Stock find(String productId) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    @Override
    public void save(Stock stock) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }
}
