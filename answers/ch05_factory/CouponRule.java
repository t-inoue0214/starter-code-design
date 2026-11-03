package com.example.orders.ch05_factory.exercise;

/**
 * クーポン1種類の割引ルールです。
 * テストがこの形を前提にしているため、メソッドの形は変えないでください。
 */
public interface CouponRule {
    /**
     * 割引額を返します。
     *
     * @param subtotal 割引前の小計（円）
     * @return 割引額（円）
     */
    int discountFor(int subtotal);
}
