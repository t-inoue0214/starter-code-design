package com.example.orders.ch07_event_driven.exercise.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参照専用のデータ（読み取りモデル）です。更新側とは別に、参照しやすい形で持ちます。
 */
public class OrderReadModel {
    // 注文の概要の表を持つフィールド
    private final Map<String, OrderSummary> summaries = new HashMap<>();
    // 顧客ごとの注文ID一覧を持つフィールド
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * 注文が成立したことを反映します。
     *
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public void apply(String orderId, String customerId, int total) {
        // 注文の概要を作り、注文IDに対応づけて保存する
        summaries.put(orderId, new OrderSummary(orderId, customerId, total));
        // 顧客の履歴の末尾に、この注文IDを追加する（履歴がなければ作る）
        idsByCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(orderId);
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要。なければ null
     */
    public OrderSummary summary(String orderId) {
        // 注文IDに対応する概要を返す（なければ null）
        return summaries.get(orderId);
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧（古い順）
     */
    public List<String> idsOf(String customerId) {
        // 呼び出し側が書き換えられないよう、コピーして返す
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }
}
