package com.example.orders.ch15_factory.exercise;

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

    /**
     * 小計に対する割引額を返します。
     *
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    @Override
    public int discountFor(int subtotal) {
        return amount;
    }
}
