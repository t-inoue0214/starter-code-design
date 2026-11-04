package com.example.orders.ch06_mvc.exercise.model;

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
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 保存された注文の表を持つフィールド
    private final Map<String, OrderRecord> records = new HashMap<>();
    // 顧客ごとの注文ID一覧を持つフィールド
    private final Map<String, List<String>> idsByCustomer = new HashMap<>();

    /**
     * @param stock 商品IDごとの在庫数
     */
    public OrderModel(Map<String, Integer> stock) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 成立した注文の記録
     */
    public OrderRecord place(Order order) {
        // 同じ注文IDが、保存済みのとき
        if (records.containsKey(order.id())) {
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
        // 請求額を、小計から会員ランクの割引を引いて求める
        int total = subtotal - rankDiscount(order.customer().rank(), subtotal);
        // 在庫と決済上限の確認
        for (OrderLine line : order.lines()) {
            // 在庫が、注文の数量に足りないとき
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                // 在庫が足りないので、在庫不足の例外を投げる
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 決済の上限を超える請求は、決済できない
        if (total > 500000) {
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 在庫の引当と記録
        for (OrderLine line : order.lines()) {
            // 商品の在庫を、注文の数量だけ減らす
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }
        // 注文ID・顧客ID・請求額から、注文の記録を作る
        OrderRecord record = new OrderRecord(order.id(), order.customer().id(), total);
        // 注文の記録を、注文IDに対応づけて保存する
        records.put(order.id(), record);
        // 顧客ごとの一覧がなければ作り、末尾に追加する（古い順を保つ）
        idsByCustomer.computeIfAbsent(record.customerId(), k -> new ArrayList<>()).add(order.id());
        // 作った注文の記録を返す
        return record;
    }

    /**
     * @param orderId 注文ID
     * @return 注文の記録
     */
    public OrderRecord find(String orderId) {
        // 注文の記録を取り出す
        OrderRecord record = records.get(orderId);
        // 注文の記録が見つからないとき
        if (record == null) {
            // 注文が見つからないことを、検証エラーとして投げる
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // 作った注文の記録を返す
        return record;
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        // 呼び出し側が一覧を書き換えても、保存内容に影響しないよう、コピーして返す
        return List.copyOf(idsByCustomer.getOrDefault(customerId, List.of()));
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank 会員ランク
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
        // 割引はない
        return 0;
    }
}
