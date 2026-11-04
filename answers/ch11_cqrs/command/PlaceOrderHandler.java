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
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 成立した注文IDの集合を持つフィールド
    private final Set<String> placedIds = new HashSet<>();

    /**
     * @param stock 商品IDごとの在庫数
     */
    public PlaceOrderHandler(Map<String, Integer> stock) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
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
        // 状態の更新
        for (OrderLine line : order.lines()) {
            // 商品の在庫を、注文の数量だけ減らす
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }
        // この注文IDを、成立済みの集合に加える
        placedIds.add(order.id());
        // 成立した注文を、更新側の結果として返す
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
