package com.example.orders.ch11_cqrs.exercise.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参照専用のデータ（読み取りモデル）です。更新側とは別に、参照しやすい形で持ちます。
 */
public class OrderReadModel {
    private final Map<String, OrderSummary> summaries = new HashMap<>();
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * 注文が成立したことを反映します。
     *
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public void apply(String orderId, String customerId, int total) {
        summaries.put(orderId, new OrderSummary(orderId, customerId, total));
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(orderId);
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要。なければ null
     */
    public OrderSummary summary(String orderId) {
        return summaries.get(orderId);
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧（古い順）
     */
    public List<String> idsOf(String customerId) {
        // 呼び出し側が一覧を書き換えても、保存内容に影響しないよう、コピーして返す
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }
}
