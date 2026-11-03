package com.example.orders.ch10_hexagonal.exercise.core;

import com.example.orders.ch10_hexagonal.exercise.core.port.inbound.OrderUseCase;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.NotificationPort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.OrderStorePort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.StockPort;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.util.List;

/**
 * 業務の中心です。ルールと手順だけを持ち、外の世界の具体的な実装を知りません。
 * TODO: ExerciseOrderApp に書かれた業務ルールと手順をここへ移し、
 *       在庫・保存・通知は、コンストラクタで受け取ったポート越しに行ってください。
 *       コンストラクタの形は、テストが前提にしているため変えないでください。
 */
public class OrderService implements OrderUseCase {
    /**
     * @param stocks       在庫のポート
     * @param store        注文保存のポート
     * @param notification 通知のポート
     */
    public OrderService(StockPort stocks, OrderStorePort store, NotificationPort notification) {
    }

    @Override
    public OrderResult place(Order order) {
        throw new UnsupportedOperationException("TODO: 第10章の課題です");
    }

    @Override
    public PlacedOrder find(String orderId) {
        throw new UnsupportedOperationException("TODO: 第10章の課題です");
    }

    @Override
    public List<String> history(String customerId) {
        throw new UnsupportedOperationException("TODO: 第10章の課題です");
    }
}
