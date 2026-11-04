package com.example.orders.ch11_cqrs.exercise.query;

import java.util.List;

/**
 * 参照（クエリ）を処理します。状態を変えず、読み取りモデルだけを使います。
 * TODO: 読み取りモデルを使って実装してください。更新側（command）は使いません。
 */
public class OrderQueryHandler {
    /**
     * @param readModel 読み取りモデル
     */
    public OrderQueryHandler(OrderReadModel readModel) {
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要。なければ OrderValidationException("注文が見つかりません: 注文ID")
     */
    public OrderSummary show(String orderId) {
        // 課題で実装するまでは、未実装として例外を投げる
        throw new UnsupportedOperationException("TODO: 第11章の課題です");
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        // 課題で実装するまでは、未実装として例外を投げる
        throw new UnsupportedOperationException("TODO: 第11章の課題です");
    }
}
