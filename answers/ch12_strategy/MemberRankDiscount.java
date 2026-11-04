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
        if (rank == MemberRank.GOLD) {
            return subtotal * 10 / 100;
        }
        if (rank == MemberRank.SILVER) {
            return subtotal * 5 / 100;
        }
        return 0;
    }
}
