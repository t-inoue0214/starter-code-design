package com.example.orders.ch12_strategy.exercise;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;

/**
 * 会員ランクによる割引です（ゴールド10%、シルバー5%）。
 */
public class MemberRankDiscount implements DiscountStrategy {
    /**
     * この割引ルールによる割引額を返します。
     *
     * @param order 注文
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    @Override
    public int discountFor(Order order, int subtotal) {
        // ランクに応じた割引率を決める
        MemberRank rank = order.customer().rank();
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
