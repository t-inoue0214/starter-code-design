package com.example.orders.ch05_factory.exercise;

/**
 * 小計の一定割合を値引きするクーポンです。
 */
public class PercentCoupon implements CouponRule {
    private final int percent;

    /**
     * @param percent 値引き率（%）
     */
    public PercentCoupon(int percent) {
        this.percent = percent;
    }

    @Override
    public int discountFor(int subtotal) {
        return subtotal * percent / 100;
    }
}
