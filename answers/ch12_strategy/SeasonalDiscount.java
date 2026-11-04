package com.example.orders.ch12_strategy.exercise;

import com.example.orders.domain.Order;

/**
 * 12月に適用される季節割引です（3%）。
 */
public class SeasonalDiscount implements DiscountStrategy {
    /**
     * この割引ルールによる割引額を返します。
     *
     * @param order 注文
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    @Override
    public int discountFor(Order order, int subtotal) {
        // 12月だけ割引する
        if (order.orderedOn().getMonthValue() == 12) {
            // 小計の3%を返す
            return subtotal * 3 / 100;
        }
        // 割引なしとして、0 を返す
        return 0;
    }
}
