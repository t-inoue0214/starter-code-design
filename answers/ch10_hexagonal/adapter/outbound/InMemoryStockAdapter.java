package com.example.orders.ch10_hexagonal.exercise.adapter.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.StockPort;
import java.util.Map;

/**
 * 在庫ポートの実装（アダプタ）です。メモリ上のMapを保存先にします。
 */
public class InMemoryStockAdapter implements StockPort {
    // 在庫の読み書き担当を持つフィールド
    private final Map<String, Integer> stocks;

    /**
     * @param stocks 商品IDごとの在庫数
     */
    public InMemoryStockAdapter(Map<String, Integer> stocks) {
        // 引数の在庫の読み書き担当を、フィールドに保存する
        this.stocks = stocks;
    }

    /**
     * 商品の在庫数を返します。
     *
     * @param productId 商品ID
     * @return 在庫数（存在しなければ0）
     */
    @Override
    public int available(String productId) {
        // 商品の在庫数を返す（登録がなければ0）
        return stocks.getOrDefault(productId, 0);
    }

    /**
     * 商品の在庫を増減します。
     *
     * @param productId 商品ID
     * @param delta 増減する数（減らすときは負）
     */
    @Override
    public void change(String productId, int delta) {
        // 現在の在庫数に増減分を足して、保存する
        stocks.put(productId, available(productId) + delta);
    }
}
