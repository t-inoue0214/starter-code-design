package com.example.orders.ch02_roles.exercise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注文の読み書きを担います。メモリ上のMapに保存します。
 */
public class OrderRepository {
    // 注文の概要の表を持つフィールド
    private final Map<String, OrderSummaryDto> summaries = new HashMap<>();
    // 顧客ごとの注文ID一覧を持つフィールド
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * @param orderId 注文ID
     * @return 保存済みなら true
     */
    public boolean exists(String orderId) {
        // その注文IDが保存済みかどうかを返す
        return summaries.containsKey(orderId);
    }

    /**
     * 注文を保存します。
     *
     * @param summary 保存する注文の概要
     */
    public void save(OrderSummaryDto summary) {
        // 注文の概要を、注文IDに対応づけて保存する
        summaries.put(summary.orderId(), summary);
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(summary.customerId(), k -> new ArrayList<>()).add(summary.orderId());
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要。なければ null
     */
    public OrderSummaryDto find(String orderId) {
        // 注文IDに対応する概要を返す（なければ null）
        return summaries.get(orderId);
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
