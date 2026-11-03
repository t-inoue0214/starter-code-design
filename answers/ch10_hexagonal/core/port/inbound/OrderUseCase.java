package com.example.orders.ch10_hexagonal.exercise.core.port.inbound;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import java.util.List;

/**
 * 入口のポートです。外の世界（コンソール、Webなど）が、アプリに依頼するための窓口を表します。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface OrderUseCase {
    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    OrderResult place(Order order);

    /**
     * @param orderId 注文ID
     * @return 注文。なければ OrderValidationException
     */
    PlacedOrder find(String orderId);

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    List<String> history(String customerId);
}
