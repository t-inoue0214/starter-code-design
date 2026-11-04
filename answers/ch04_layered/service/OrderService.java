package com.example.orders.ch04_layered.exercise.service;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch04_layered.exercise.persistence.OrderRecord;
import com.example.orders.ch04_layered.exercise.persistence.OrderRepository;
import com.example.orders.ch04_layered.exercise.persistence.StockRepository;
import java.util.List;

/**
 * 注文の業務ルールを扱います（業務層）。表示のことも、保存の細部も知りません。
 */
public class OrderService {
    // 在庫の読み書き担当を持つフィールド
    private final StockRepository stocks;
    // 注文の表を持つフィールド
    private final OrderRepository orders;
    // 通知の担当を持つフィールド
    private final OrderNotifier notifier;

    /**
     * @param stocks   在庫の保存先
     * @param orders   注文の保存先
     * @param notifier 通知
     */
    public OrderService(StockRepository stocks, OrderRepository orders, OrderNotifier notifier) {
        // 引数の在庫の読み書き担当を、フィールドに保存する
        this.stocks = stocks;
        // 引数の注文の表を、フィールドに保存する
        this.orders = orders;
        // 引数の通知の担当を、フィールドに保存する
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
            // 注文IDの重複を、検証エラーとして投げる
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        // 金額計算
        int subtotal = 0;
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 単価 × 数量を、小計に加える
            subtotal += line.unitPrice() * line.quantity();
        }
        // 会員ランクごとの割引を求める
        int discount = rankDiscount(order.customer().rank(), subtotal);
        // 請求額を、小計から割引額を引いて求める
        int total = subtotal - discount;
        // 在庫の確認と、決済の上限の確認
        for (OrderLine line : order.lines()) {
            // 在庫が、注文の数量に足りないとき
            if (stocks.available(line.productId()) < line.quantity()) {
                // 在庫が足りないので、在庫不足の例外を投げる
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 決済の上限を超える請求は、決済できない
        if (total > 500000) {
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 在庫の引当と注文の保存、通知
        for (OrderLine line : order.lines()) {
            // 検証がすべて通ってから、在庫を減らす（途中で失敗しても、在庫は減らない）
            stocks.change(line.productId(), -line.quantity());
        }
        // ★重要★ 保存は永続化層に任せる（業務層は保存の細部を知らない）
        orders.save(order, total);
        // 注文の成立を、通知の担当に知らせる
        notifier.orderPlaced(order, total);
        // 注文ID・小計・割引額・請求額を、結果として返す
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /**
     * 注文を取り出します。
     *
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary find(String orderId) {
        // 永続化層から、注文の記録を取り出す
        OrderRecord record = orders.find(orderId);
        // 注文の記録が見つからないとき
        if (record == null) {
            // 注文が見つからないことを、検証エラーとして投げる
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // ★重要★ 永続化層の型を、業務層の型に変換して返す
        return new OrderSummary(record.orderId(), record.customerId(), record.total());
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        // 保存先から、顧客の注文ID一覧を取り出して返す
        return orders.idsOf(customerId);
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank     会員ランク
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    private int rankDiscount(MemberRank rank, int subtotal) {
        // ゴールド会員なら
        if (rank == MemberRank.GOLD) {
            // 小計の10%を返す
            return subtotal * 10 / 100;
        }
        // シルバー会員なら
        if (rank == MemberRank.SILVER) {
            // 小計の5%を返す
            return subtotal * 5 / 100;
        }
        // 割引なしとして、0 を返す
        return 0;
    }
}
