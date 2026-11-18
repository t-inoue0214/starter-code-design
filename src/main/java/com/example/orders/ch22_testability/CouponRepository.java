package com.example.orders.ch22_testability;

import java.util.List;

/**
 * クーポンの保存先です。本番ではデータベース、テストではメモリ上の偽物を渡します。
 */
public interface CouponRepository {

    /**
     * クーポンを保存します。
     *
     * @param coupon 保存するクーポン
     */
    void save(Coupon coupon);

    /**
     * 顧客のクーポンを探します。
     *
     * @param customerId 顧客ID
     * @return 顧客のクーポンの一覧（なければ空）
     */
    List<Coupon> findByCustomer(String customerId);
}
