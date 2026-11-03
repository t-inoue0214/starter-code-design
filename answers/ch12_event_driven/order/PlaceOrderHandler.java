package com.example.orders.ch12_event_driven.exercise.order;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch12_event_driven.exercise.event.EventBus;
import com.example.orders.ch12_event_driven.exercise.event.OrderPlacedEvent;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 注文の受け付けを処理します。成立したら、イベントを出すだけです。通知や参照データの更新は、誰が行うのか知りません。
 */
public class PlaceOrderHandler {
    private final Map<String, Integer> stock;
    private final EventBus bus;
    private final Set<String> placedIds = new HashSet<>();

    /**
     * @param stock 商品IDごとの在庫数
     * @param bus   イベントバス
     */
    public PlaceOrderHandler(Map<String, Integer> stock, EventBus bus) {
        this.stock = stock;
        this.bus = bus;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    public OrderResult place(Order order) {
        if (placedIds.contains(order.id())) {
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        // 金額計算
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        int discount = rankDiscount(order.customer().rank(), subtotal);
        int total = subtotal - discount;
        // 在庫と決済上限の確認
        for (OrderLine line : order.lines()) {
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 状態の更新
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }
        placedIds.add(order.id());
        // 「成立した」という事実を知らせる（誰が受け取るかは知らない）
        bus.publish(new OrderPlacedEvent(order.id(), order.customer().id(), total));
        return new OrderResult(order.id(), subtotal, discount, total);
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
