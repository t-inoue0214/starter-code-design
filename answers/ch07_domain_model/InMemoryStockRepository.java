package com.example.orders.ch07_domain_model.exercise;

import java.util.Map;

/**
 * Map を保存先にした StockRepository です。
 */
public class InMemoryStockRepository implements StockRepository {
    private final Map<String, Integer> stocks;

    /**
     * @param stocks 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public InMemoryStockRepository(Map<String, Integer> stocks) {
        this.stocks = stocks;
    }

    @Override
    public Stock find(String productId) {
        // 存在しない商品は在庫0として返す
        return new Stock(productId, stocks.getOrDefault(productId, 0));
    }

    @Override
    public void save(Stock stock) {
        stocks.put(stock.productId(), stock.quantity());
    }
}
