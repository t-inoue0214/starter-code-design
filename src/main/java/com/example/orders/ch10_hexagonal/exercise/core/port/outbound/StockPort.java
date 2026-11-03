package com.example.orders.ch10_hexagonal.exercise.core.port.outbound;


/**
 * 出口のポート（在庫）です。業務の中心が「在庫がほしい」と外へ頼むための窓口を、中心の側で定義します。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface StockPort {
    /**
     * @param productId 商品ID
     * @return 在庫数（なければ0）
     */
    int available(String productId);

    /**
     * @param productId 商品ID
     * @param delta     増減する数（減らすときは負）
     */
    void change(String productId, int delta);
}
