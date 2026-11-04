package com.example.orders.ch05_ecb.exercise.entity;

import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OutOfStockException;
import java.util.List;
import java.util.Map;

/**
 * 在庫という業務の概念です（Entity）。在庫の数と、「足りなければ引き当てられない」というルールを持ちます。
 */
public class Stock {
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> quantities;

    /**
     * @param quantities 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public Stock(Map<String, Integer> quantities) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.quantities = quantities;
    }

    /**
     * 全明細の在庫が足りるかを確かめます。
     *
     * @param lines 注文の明細
     * @throws OutOfStockException 1つでも足りないとき
     */
    public void assertAvailable(List<OrderLine> lines) {
        // 明細を1行ずつ取り出して処理する
        for (OrderLine line : lines) {
            // ★重要★ 「足りるか」のルールは、在庫（Entity）が自分で持つ
            if (quantities.getOrDefault(line.productId(), 0) < line.quantity()) {
                // 在庫が足りないので、在庫不足の例外を投げる
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
    }

    /**
     * 全明細の在庫を引き当てます。事前に {@link #assertAvailable} を呼んでください。
     *
     * @param lines 注文の明細
     */
    public void reserve(List<OrderLine> lines) {
        // 明細を1行ずつ取り出して処理する
        for (OrderLine line : lines) {
            // 商品の在庫を、注文の数量だけ減らす
            quantities.put(line.productId(), quantities.get(line.productId()) - line.quantity());
        }
    }
}
