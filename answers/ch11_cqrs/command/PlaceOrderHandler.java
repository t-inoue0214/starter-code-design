package com.example.orders.ch11_cqrs.exercise.command;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 注文の受け付け（コマンド）を処理します。ルールの確認と状態の更新に集中し、参照用のデータは持ちません。
 */
public class PlaceOrderHandler {
    private final Map<String, Integer> stock;
    private final Set<String> placedIds = new HashSet<>();

    /**
     * @param stock 商品IDごとの在庫数
     */
    public PlaceOrderHandler(Map<String, Integer> stock) {
        this.stock = stock;
    }

    /**
     * 注文を受け付けて、状態を更新します。
     *
     * @param order 注文
     * @return 成立した注文の結果
     */
    public PlacedOrder place(Order order) {
        // ★重要★ 重複の確認（更新側が必要とする最小限の状態）
        if (placedIds.contains(order.id())) {
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
        // 決済の上限を超える請求は、決済できない
        if (total > 500000) {
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 状態の更新
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }
        placedIds.add(order.id());
        return new PlacedOrder(order.id(), order.customer().id(), total);
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
