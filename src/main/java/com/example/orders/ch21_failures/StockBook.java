package com.example.orders.ch21_failures;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 商品の単価と在庫を持つ、メモリ上の台帳です。本番では、データベースを読む部品に当たります。
 */
public final class StockBook {
    // 商品IDごとの単価を持つフィールド
    private final Map<String, Integer> prices = new HashMap<>();
    // 商品IDごとの在庫の数を持つフィールド
    private final Map<String, Integer> stocks = new HashMap<>();

    /**
     * 商品を登録します。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param stock     在庫の数
     */
    public void register(String productId, int unitPrice, int stock) {
        // 単価を登録する
        prices.put(productId, unitPrice);
        // 在庫の数を登録する
        stocks.put(productId, stock);
    }

    /**
     * 商品の単価を探します。
     *
     * @param productId 商品ID
     * @return 単価。商品がなければ空
     */
    public Optional<Integer> unitPriceOf(String productId) {
        // ★重要★ 「見つからない」は起こりうるので、null ではなく Optional で返して、確認を促す
        return Optional.ofNullable(prices.get(productId));
    }

    /**
     * 在庫の数を返します。
     *
     * @param productId 商品ID
     * @return 在庫の数（登録がなければ0）
     */
    public int stockOf(String productId) {
        // 登録がなければ0とみなして返す
        return stocks.getOrDefault(productId, 0);
    }

    /**
     * 在庫を減らします。
     *
     * @param productId 商品ID
     * @param quantity  減らす数
     */
    public void take(String productId, int quantity) {
        // 在庫から数を引いて、保存し直す
        stocks.put(productId, stockOf(productId) - quantity);
    }
}
