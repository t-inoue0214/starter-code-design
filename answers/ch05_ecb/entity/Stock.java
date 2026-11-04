package com.example.orders.ch05_ecb.exercise.entity;

import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OutOfStockException;
import java.util.List;
import java.util.Map;

/**
 * 在庫という業務の概念です（Entity）。在庫の数と、「足りなければ引き当てられない」というルールを持ちます。
 */
public class Stock {
    private final Map<String, Integer> quantities;

    /**
     * @param quantities 商品IDごとの在庫数（このMapを直接読み書きする）
     */
    public Stock(Map<String, Integer> quantities) {
        this.quantities = quantities;
    }

    /**
     * 全明細の在庫が足りるかを確かめます。
     *
     * @param lines 注文の明細
     * @throws OutOfStockException 1つでも足りないとき
     */
    public void assertAvailable(List<OrderLine> lines) {
        for (OrderLine line : lines) {
            if (quantities.getOrDefault(line.productId(), 0) < line.quantity()) {
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
        for (OrderLine line : lines) {
            quantities.put(line.productId(), quantities.get(line.productId()) - line.quantity());
        }
    }
}
