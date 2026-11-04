package com.example.orders.ch07_event_driven.exercise.query;

import com.example.orders.domain.OrderValidationException;
import java.util.List;

/**
 * 参照（クエリ）を処理します。状態を変えず、読み取りモデルだけを使います。
 */
public class OrderQueryHandler {
    private final OrderReadModel readModel;

    /**
     * @param readModel 読み取りモデル
     */
    public OrderQueryHandler(OrderReadModel readModel) {
        this.readModel = readModel;
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary show(String orderId) {
        OrderSummary summary = readModel.summary(orderId);
        if (summary == null) {
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        return summary;
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        return readModel.idsOf(customerId);
    }
}
