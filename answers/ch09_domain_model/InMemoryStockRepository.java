package com.example.orders.ch09_domain_model.exercise;

import java.util.Map;

/**
 * Map を保存先にした StockRepository です。
 */
public class InMemoryStockRepository implements StockRepository {
    // 在庫の読み書き担当を持つフィールド
    private final Map<String, Integer> stocks;

    /**
     * @param stocks 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public InMemoryStockRepository(Map<String, Integer> stocks) {
        // 引数の在庫の読み書き担当を、フィールドに保存する
        this.stocks = stocks;
    }

    /**
     * 商品の在庫を取り出します。
     *
     * @param productId 商品ID
     * @return 在庫
     */
    @Override
    public Stock find(String productId) {
        // 存在しない商品は在庫0として返す
        return new Stock(productId, stocks.getOrDefault(productId, 0));
    }

    /**
     * 在庫を保存します。
     *
     * @param stock 商品IDごとの在庫数
     */
    @Override
    public void save(Stock stock) {
        // 在庫の数を、元のMapに書き戻す
        stocks.put(stock.productId(), stock.quantity());
    }
}
