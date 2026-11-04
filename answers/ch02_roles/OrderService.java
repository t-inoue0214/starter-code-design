package com.example.orders.ch02_roles.exercise;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.util.List;

/**
 * 注文の業務ロジック（ルールと手順）を担います。入力の形式も、出力の方法も知りません。
 */
public class OrderService {
    private final StockRepository stocks;
    private final OrderRepository orders;
    private final ConsoleNotifier notifier;

    /**
     * 部品をコンストラクタで受け取ります（依存性注入）。
     *
     * @param stocks   在庫の読み書き
     * @param orders   注文の読み書き
     * @param notifier 通知
     */
    public OrderService(StockRepository stocks, OrderRepository orders, ConsoleNotifier notifier) {
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
        // 会員ランクごとの割引を求める
        int discount = rankDiscount(order.customer().rank(), subtotal);
        int total = subtotal - discount;
        // 在庫の確認と、決済の上限の確認
        for (OrderLine line : order.lines()) {
            if (stocks.available(line.productId()) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 決済の上限を超える請求は、決済できない
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 在庫の引当、注文の保存、通知
        for (OrderLine line : order.lines()) {
            // 検証がすべて通ってから、在庫を減らす（途中で失敗しても、在庫は減らない）
            stocks.change(line.productId(), -line.quantity());
        }
        orders.save(new OrderSummaryDto(order.id(), order.customer().id(), total));
        notifier.orderPlaced(order, total);
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummaryDto find(String orderId) {
        OrderSummaryDto summary = orders.find(orderId);
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
        return orders.idsOf(customerId);
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank 会員ランク
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
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
