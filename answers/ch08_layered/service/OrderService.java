package com.example.orders.ch08_layered.exercise.service;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch08_layered.exercise.persistence.OrderRecord;
import com.example.orders.ch08_layered.exercise.persistence.OrderRepository;
import com.example.orders.ch08_layered.exercise.persistence.StockRepository;
import java.util.List;

/**
 * 注文の業務ルールを扱います（業務層）。表示のことも、保存の細部も知りません。
 */
public class OrderService {
    private final StockRepository stocks;
    private final OrderRepository orders;
    private final OrderNotifier notifier;

    /**
     * @param stocks   在庫の保存先
     * @param orders   注文の保存先
     * @param notifier 通知
     */
    public OrderService(StockRepository stocks, OrderRepository orders, OrderNotifier notifier) {
        this.stocks = stocks;
        this.orders = orders;
        this.notifier = notifier;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    public OrderResult place(Order order) {
        // 重複の確認
        if (orders.exists(order.id())) {
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        // 金額計算
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        int discount = rankDiscount(order.customer().rank(), subtotal);
        int total = subtotal - discount;
        // 在庫の確認と、決済の上限の確認
        for (OrderLine line : order.lines()) {
            if (stocks.available(line.productId()) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 在庫の引当と注文の保存、通知
        for (OrderLine line : order.lines()) {
            stocks.change(line.productId(), -line.quantity());
        }
        orders.save(order, total);
        notifier.orderPlaced(order, total);
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /**
     * 注文を取り出します。
     *
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary find(String orderId) {
        OrderRecord record = orders.find(orderId);
        if (record == null) {
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // 永続化層の型を、業務層の型に変換して返す
        return new OrderSummary(record.orderId(), record.customerId(), record.total());
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        return orders.idsOf(customerId);
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
