package com.example.orders.ch12_strategy.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderValidationException;

/**
 * クーポンによる割引です（WELCOME500 は500円引き、SAVE10 は10%引き）。
 */
public class CouponDiscount implements DiscountStrategy {
    /**
     * この割引ルールによる割引額を返します。
     *
     * @param order 注文
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    @Override
    public int discountFor(Order order, int subtotal) {
        // クーポンがなければ割引なし
        String coupon = order.couponCode();
        // クーポンの指定がないとき
        if (coupon == null) {
            // 割引なしとして、0 を返す
            return 0;
        }
        // WELCOME500 は、500円引きのクーポン
        if (coupon.equals("WELCOME500")) {
            // WELCOME500 は、500円引き
            return 500;
        }
        // SAVE10 は、小計の10%引きのクーポン
        if (coupon.equals("SAVE10")) {
            // 小計の10%を返す
            return subtotal * 10 / 100;
        }
        // 登録されていないクーポンは、検証エラーとして投げる
        throw new OrderValidationException("未知のクーポンです: " + coupon);
    }
}
