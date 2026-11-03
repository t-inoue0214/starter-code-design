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

    @Override
    public boolean exists(String orderId) {
        return orders.containsKey(orderId);
    }

    @Override
    public void save(PlacedOrder order) {
        orders.put(order.orderId(), order);
        idsByCustomer.computeIfAbsent(order.customerId(), k -> new ArrayList<>()).add(order.orderId());
    }

    @Override
    public PlacedOrder find(String orderId) {
        return orders.get(orderId);
    }

    @Override
    public List<String> idsOf(String customerId) {
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }
}
