package com.example.orders.ch04_layered.exercise.persistence;

import com.example.orders.domain.Order;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注文の保存先です（永続化層）。メモリ上のMapに保存します。
 */
public class OrderRepository {
    private final Map<String, OrderRecord> records = new HashMap<>();
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * @param orderId 注文ID
     * @return 保存済みなら true
     */
    public boolean exists(String orderId) {
        return records.containsKey(orderId);
    }

    /**
     * 注文を保存します。
     *
     * @param order 注文
     * @param total 請求額（円）
     */
    public void save(Order order, int total) {
        String customerId = order.customer().id();
        records.put(order.id(), new OrderRecord(order.id(), customerId, total));
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(order.id());
    }

    /**
     * @param orderId 注文ID
     * @return 記録。なければ null
     */
    public OrderRecord find(String orderId) {
        return records.get(orderId);
    }

    /**
     * @param customerId 顧客ID
     * @return 注文IDの一覧（古い順）
     */
    public List<String> idsOf(String customerId) {
        // 呼び出し側が一覧を書き換えても、保存内容に影響しないよう、コピーして返す
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }
}
