package com.example.orders.ch10_hexagonal.exercise.core.port.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import java.util.List;

/**
 * 出口のポート（注文の保存）です。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface OrderStorePort {
    /**
     * @param orderId 注文ID
     * @return 保存済みなら true
     */
    boolean exists(String orderId);

    /**
     * @param order 保存する注文
     */
    void save(PlacedOrder order);

    /**
     * @param orderId 注文ID
     * @return 注文。なければ null
     */
    PlacedOrder find(String orderId);

    /**
     * @param customerId 顧客ID
     * @return 注文IDの一覧（古い順）
     */
    List<String> idsOf(String customerId);
}
