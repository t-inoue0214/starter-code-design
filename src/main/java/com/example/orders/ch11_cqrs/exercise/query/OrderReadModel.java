package com.example.orders.ch11_cqrs.exercise.query;

import java.util.List;

/**
 * 参照専用のデータ（読み取りモデル）です。更新側とは別に、参照しやすい形で持ちます。
 * TODO: 注文ごとの概要と、顧客ごとの注文IDの一覧（古い順）を持つように実装してください。
 *       テストがこの形を前提にしているため、メソッドの形は変えないでください。
 */
public class OrderReadModel {
    /**
     * 注文が成立したことを反映します。
     *
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param total      請求額（円）
     */
    public void apply(String orderId, String customerId, int total) {
        throw new UnsupportedOperationException("TODO: 第11章の課題です");
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要。なければ null
     */
    public OrderSummary summary(String orderId) {
        throw new UnsupportedOperationException("TODO: 第11章の課題です");
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧（古い順）
     */
    public List<String> idsOf(String customerId) {
        throw new UnsupportedOperationException("TODO: 第11章の課題です");
    }
}
