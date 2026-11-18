package com.example.orders.ch22_testability;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メモリ上の保存先です。インスタンスごとに中身が別なので、テストどうしで中身が混ざりません。
 */
public final class InMemoryCouponRepository implements CouponRepository {
    // 保存したクーポンを持つフィールド
    private final List<Coupon> coupons = new ArrayList<>();

    /**
     * クーポンを保存します。
     *
     * @param coupon 保存するクーポン
     */
    @Override
    public void save(Coupon coupon) {
        // 一覧に足す
        coupons.add(coupon);
    }

    /**
     * 顧客のクーポンを探します。
     *
     * @param customerId 顧客ID
     * @return 顧客のクーポンの一覧（なければ空）
     */
    @Override
    public List<Coupon> findByCustomer(String customerId) {
        // 顧客IDが同じものだけを集めて返す
        return coupons.stream().filter(c -> c.customerId().equals(customerId)).collect(Collectors.toUnmodifiableList());
    }
}
