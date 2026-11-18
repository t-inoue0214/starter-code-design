package com.example.orders.ch20_visibility.order;

/**
 * 注文の状態です。外から状態を読むために、public にしています。
 */
public enum OrderStatus {
    /** 下書き。商品を足せる。 */
    DRAFT,
    /** 確定済み。商品を足せない。 */
    PLACED
}
