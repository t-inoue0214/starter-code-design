package com.example.orders.ch10_hexagonal.exercise.core;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch10_hexagonal.exercise.core.port.inbound.OrderUseCase;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.NotificationPort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.OrderStorePort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.StockPort;
import java.util.List;

/**
 * 業務の中心です。ルールと手順だけを持ち、外の世界（入力・保存・通知）の具体的な実装を知りません。
 * 外へ頼むときは、中心の側で定義したポート（インターフェース）だけを使います。
 */
public class OrderService implements OrderUseCase {
    private final StockPort stocks;
    private final OrderStorePort store;
    private final NotificationPort notification;

    /**
     * @param stocks       在庫のポート
     * @param store        注文保存のポート
     * @param notification 通知のポート
     */
    public OrderService(StockPort stocks, OrderStorePort store, NotificationPort notification) {
        this.stocks = stocks;
        this.store = store;
        this.notification = notification;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult place(Order order) {
        // 同じ注文IDは受け付けない（二重注文を防ぐ）
        if (store.exists(order.id())) {
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
        // 在庫と決済上限の確認
        for (OrderLine line : order.lines()) {
            if (stocks.available(line.productId()) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 決済の上限を超える請求は、決済できない
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // ★重要★ 在庫の引当、保存、通知（すべてポート越し）
        for (OrderLine line : order.lines()) {
            // 検証がすべて通ってから、在庫を減らす（途中で失敗しても、在庫は減らない）
            stocks.change(line.productId(), -line.quantity());
        }
        PlacedOrder placed = new PlacedOrder(order.id(), order.customer().id(), total);
        store.save(placed);
        // ★重要★ 通知もポート越し。中心は、通知の手段（コンソール、メールなど）を知らない
        notification.orderPlaced(placed);
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /**
     * 注文を取り出します。
     *
     * @param orderId 注文ID
     * @return 注文
     */
    @Override
    public PlacedOrder find(String orderId) {
        PlacedOrder placed = store.find(orderId);
        if (placed == null) {
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        return placed;
    }

    /**
     * 顧客の注文IDの一覧を返します。
     *
     * @param customerId 顧客ID
     * @return 注文IDの一覧（古い順）
     */
    @Override
    public List<String> history(String customerId) {
        return store.idsOf(customerId);
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank     会員ランク
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
