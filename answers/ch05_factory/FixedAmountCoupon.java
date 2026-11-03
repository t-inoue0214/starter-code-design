package com.example.orders.ch05_factory.exercise;

/**
 * 固定額を値引きするクーポンです。
 */
public class FixedAmountCoupon implements CouponRule {
    private final int amount;

    /**
     * @param amount 値引き額（円）
     */
    public FixedAmountCoupon(int amount) {
        this.amount = amount;
    }

    @Override
    public int discountFor(int subtotal) {
        return amount;
    }
}
