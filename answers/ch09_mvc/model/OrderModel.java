package com.example.orders.ch09_mvc.exercise.model;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * アプリのデータと業務ルールを持ちます（Model）。入力の形式も、出力の形も知りません。
 */
public class OrderModel {
    private final Map<String, Integer> stock;
    private final Map<String, OrderRecord> records = new HashMap<>();
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * @param stock 商品IDごとの在庫数
     */
    public OrderModel(Map<String, Integer> stock) {
        this.stock = stock;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 成立した注文の記録
     */
    public OrderRecord place(Order order) {
        if (records.containsKey(order.id())) {
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        // 金額計算
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        int total = subtotal - rankDiscount(order.customer().rank(), subtotal);
        // 在庫と決済上限の確認
        for (OrderLine line : order.lines()) {
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 在庫の引当と記録
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }
        OrderRecord record = new OrderRecord(order.id(), order.customer().id(), total);
        records.put(order.id(), record);
        idsByCustomer.computeIfAbsent(record.customerId(), k -> new ArrayList<>()).add(order.id());
        return record;
    }

    /**
     * @param orderId 注文ID
     * @return 注文の記録
     */
    public OrderRecord find(String orderId) {
        OrderRecord record = records.get(orderId);
        if (record == null) {
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        return record;
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }

    private int rankDiscount(MemberRank rank, int subtotal) {
        if (rank == MemberRank.GOLD) {
            return subtotal * 10 / 100;
        }
        if (rank == MemberRank.SILVER) {
            return subtotal * 5 / 100;
        }
        return 0;
    }
}
