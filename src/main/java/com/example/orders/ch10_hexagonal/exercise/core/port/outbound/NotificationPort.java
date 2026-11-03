package com.example.orders.ch10_hexagonal.exercise.core.port.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;

/**
 * 出口のポート（通知）です。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface NotificationPort {
    /**
     * 注文が成立したことを知らせます。
     *
     * @param order 成立した注文
     */
    void orderPlaced(PlacedOrder order);
}
