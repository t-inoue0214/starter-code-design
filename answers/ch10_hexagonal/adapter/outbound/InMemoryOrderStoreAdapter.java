package com.example.orders.ch10_hexagonal.exercise.adapter.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.OrderStorePort;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注文保存ポートの実装（アダプタ）です。メモリ上のMapに保存します。
 */
public class InMemoryOrderStoreAdapter implements OrderStorePort {
    private final Map<String, PlacedOrder> orders = new HashMap<>();
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * 注文IDが保存済みかを返します。
     *
     * @param orderId 注文ID
     * @return 保存済みなら true
     */
    @Override
    public boolean exists(String orderId) {
        return orders.containsKey(orderId);
    }

    /**
     * 注文を保存します。
     *
     * @param order 注文
     */
    @Override
    public void save(PlacedOrder order) {
        orders.put(order.orderId(), order);
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(order.customerId(), k -> new ArrayList<>()).add(order.orderId());
    }

    /**
     * 注文を取り出します。
     *
     * @param orderId 注文ID
     * @return 注文
     */
    @Override
    public PlacedOrder find(String orderId) {
        return orders.get(orderId);
    }

    /**
     * 顧客の注文IDの一覧を返します。
     *
     * @param customerId 顧客ID
     * @return 注文IDの一覧（古い順）
     */
    @Override
    public List<String> idsOf(String customerId) {
        // 呼び出し側が一覧を書き換えても、保存内容に影響しないよう、コピーして返す
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }
}
