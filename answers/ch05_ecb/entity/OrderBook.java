package com.example.orders.ch05_ecb.exercise.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成立した注文の台帳です（Entity）。「同じ注文IDは1つだけ」というルールを持ちます。
 */
public class OrderBook {
    private final Map<String, PlacedOrder> orders = new HashMap<>();
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * @param orderId 注文ID
     * @return 登録済みなら true
     */
    public boolean contains(String orderId) {
        return orders.containsKey(orderId);
    }

    /**
     * @param placed 登録する注文
     */
    public void add(PlacedOrder placed) {
        orders.put(placed.orderId(), placed);
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(placed.customerId(), k -> new ArrayList<>()).add(placed.orderId());
    }

    /**
     * @param orderId 注文ID
     * @return 注文。なければ null
     */
    public PlacedOrder find(String orderId) {
        return orders.get(orderId);
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
