package com.example.orders.ch10_hexagonal.exercise.adapter.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.StockPort;
import java.util.Map;

/**
 * 在庫ポートの実装（アダプタ）です。メモリ上のMapを保存先にします。
 */
public class InMemoryStockAdapter implements StockPort {
    private final Map<String, Integer> stocks;

    /**
     * @param stocks 商品IDごとの在庫数
     */
    public InMemoryStockAdapter(Map<String, Integer> stocks) {
        this.stocks = stocks;
    }

    @Override
    public int available(String productId) {
        return stocks.getOrDefault(productId, 0);
    }

    @Override
    public void change(String productId, int delta) {
        stocks.put(productId, available(productId) + delta);
    }
}
