package com.example.orders.ch09_domain_model.exercise;

/**
 * 在庫の保存先を表す窓口（Repository）です。
 * 業務ルールは保存先を知らず、この窓口だけを知ります。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface StockRepository {
    /**
     * 商品の在庫を取り出します。存在しない商品は、在庫0として返します。
     *
     * @param productId 商品ID
     * @return 在庫（null にはならない）
     */
    Stock find(String productId);

    /**
     * 在庫を保存します。
     *
     * @param stock 保存する在庫
     */
    void save(Stock stock);
}
