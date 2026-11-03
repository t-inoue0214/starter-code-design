package com.example.orders.ch02_strategy.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderValidationException;

/**
 * クーポンによる割引です（WELCOME500 は500円引き、SAVE10 は10%引き）。
 */
public class CouponDiscount implements DiscountStrategy {
    @Override
    public int discountFor(Order order, int subtotal) {
        // クーポンがなければ割引なし
        String coupon = order.couponCode();
        if (coupon == null) {
            return 0;
        }
        if (coupon.equals("WELCOME500")) {
            return 500;
        }
        if (coupon.equals("SAVE10")) {
            return subtotal * 10 / 100;
        }
        throw new OrderValidationException("未知のクーポンです: " + coupon);
    }
}
