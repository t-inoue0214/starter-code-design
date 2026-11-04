package com.example.orders.ch05_ecb.exercise.entity;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.PaymentDeclinedException;

/**
 * 成立した注文という業務の概念です（Entity）。金額の計算と、決済上限のルールを持ちます。
 */
public class PlacedOrder {
    // PAYMENT_LIMITを持つフィールド
    private static final int PAYMENT_LIMIT = 500000;

    // 注文IDを持つフィールド
    private final String orderId;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 小計（円）を持つフィールド
    private final int subtotal;
    // 割引額（円）を持つフィールド
    private final int discount;

    /**
     * 成立した注文を作ります。外からは from で作ります。
     *
     * @param orderId 注文ID
     * @param customerId 顧客ID
     * @param subtotal 小計（円）
     * @param discount 割引額（円）
     */
    private PlacedOrder(String orderId, String customerId, int subtotal, int discount) {
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の小計（円）を、フィールドに保存する
        this.subtotal = subtotal;
        // 引数の割引額（円）を、フィールドに保存する
        this.discount = discount;
    }

    /**
     * 注文から、成立した注文を作ります。金額を計算し、決済上限を超えていれば拒否します。
     *
     * @param order 注文
     * @return 成立した注文
     * @throws PaymentDeclinedException 請求額が決済上限を超えるとき
     */
    public static PlacedOrder from(Order order) {
        // 小計を0から数え始める
        int subtotal = 0;
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 単価 × 数量を、小計に加える
            subtotal += line.unitPrice() * line.quantity();
        }
        // 注文ID・顧客ID・小計・割引額から、成立した注文を作る
        PlacedOrder placed = new PlacedOrder(order.id(), order.customer().id(), subtotal,
                rankDiscount(order.customer().rank(), subtotal));
        // ★重要★ 決済上限のルールは、この Entity が自分で持つ
        if (placed.total() > PAYMENT_LIMIT) {
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 作った注文を返す
        return placed;
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank 会員ランク
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    private static int rankDiscount(MemberRank rank, int subtotal) {
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

    // 注文IDを返す
    public String orderId() { return orderId; }
    // 顧客IDを返す
    public String customerId() { return customerId; }
    // 小計（円）を返す
    public int subtotal() { return subtotal; }
    // 割引額（円）を返す
    public int discount() { return discount; }
    // 請求額は、小計から割引額を引いたもの
    public int total() { return subtotal - discount; }
}
