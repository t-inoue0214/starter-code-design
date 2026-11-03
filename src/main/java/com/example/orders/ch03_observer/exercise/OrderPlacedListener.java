package com.example.orders.ch03_observer.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;

/**
 * 注文が成立したときに呼ばれる通知先（Observer）です。
 * テストがこの形を前提にしているため、メソッドの形は変えないでください。
 */
public interface OrderPlacedListener {
    /**
     * 注文が成立したときに呼ばれます。
     *
     * @param order  成立した注文
     * @param result 金額の内訳
     */
    void onOrderPlaced(Order order, OrderResult result);
}
