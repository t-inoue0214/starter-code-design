package com.example.orders.ch15_factory.exercise;

/**
 * 小計の一定割合を値引きするクーポンです。
 */
public class PercentCoupon implements CouponRule {
    // percentを持つフィールド
    private final int percent;

    /**
     * @param percent 値引き率（%）
     */
    public PercentCoupon(int percent) {
        // 引数のpercentを、フィールドに保存する
        this.percent = percent;
    }

    /**
     * 小計に対する割引額を返します。
     *
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    @Override
    public int discountFor(int subtotal) {
        // 小計に割合をかけて返す（端数は切り捨て）
        return subtotal * percent / 100;
    }
}
