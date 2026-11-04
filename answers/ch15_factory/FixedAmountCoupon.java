package com.example.orders.ch15_factory.exercise;

/**
 * 固定額を値引きするクーポンです。
 */
public class FixedAmountCoupon implements CouponRule {
    // amountを持つフィールド
    private final int amount;

    /**
     * @param amount 値引き額（円）
     */
    public FixedAmountCoupon(int amount) {
        // 引数のamountを、フィールドに保存する
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
        // 定額を返す
        return amount;
    }
}
