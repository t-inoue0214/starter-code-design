package com.example.orders.ch02_strategy.exercise;

import com.example.orders.domain.Order;

/**
 * 12月に適用される季節割引です（3%）。
 */
public class SeasonalDiscount implements DiscountStrategy {
    @Override
    public int discountFor(Order order, int subtotal) {
        // 12月だけ割引する
        if (order.orderedOn().getMonthValue() == 12) {
            return subtotal * 3 / 100;
        }
        return 0;
    }
}
