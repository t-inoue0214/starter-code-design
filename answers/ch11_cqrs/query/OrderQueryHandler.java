package com.example.orders.ch11_cqrs.exercise.query;

import com.example.orders.domain.OrderValidationException;
import java.util.List;

/**
 * 参照（クエリ）を処理します。状態を変えず、読み取りモデルだけを使います。
 */
public class OrderQueryHandler {
    // 読み取りモデルを持つフィールド
    private final OrderReadModel readModel;

    /**
     * @param readModel 読み取りモデル
     */
    public OrderQueryHandler(OrderReadModel readModel) {
        // 引数の読み取りモデルを、フィールドに保存する
        this.readModel = readModel;
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary show(String orderId) {
        // 読み取りモデルから、注文の概要を取り出す
        OrderSummary summary = readModel.summary(orderId);
        // 注文の概要が見つからないとき
        if (summary == null) {
            // 注文が見つからないことを、検証エラーとして投げる
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // 見つかった注文の概要を返す
        return summary;
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        // 読み取りモデルから、顧客の注文ID一覧を返す
        return readModel.idsOf(customerId);
    }
}
