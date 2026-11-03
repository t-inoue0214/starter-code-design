package com.example.orders.ch02_strategy.exercise;

import com.example.orders.domain.Order;

/**
 * 割引ルール1種類を表す戦略（Strategy）です。
 * テストがこの形を前提にしているため、メソッドの形は変えないでください。
 */
public interface DiscountStrategy {
    /**
     * このルールによる割引額を返します。
     *
     * @param order    注文
     * @param subtotal 割引前の小計（円）
     * @return 割引額（円）。割引がなければ 0
     */
    int discountFor(Order order, int subtotal);
}
