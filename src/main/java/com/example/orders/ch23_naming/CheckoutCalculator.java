package com.example.orders.ch23_naming;

/**
 * 支払額を計算するクラスです。名前の「Calculator」が、入れてよいものを計算に限っています。
 */
public final class CheckoutCalculator {
    // 会員の割引率（%）
    static final int MEMBER_DISCOUNT_PERCENT = 5;
    // 消費税率（%）
    static final int TAX_PERCENT = 10;

    /**
     * 割引と税を反映した、支払額を計算します。
     *
     * @param order 注文
     * @param rank  顧客の区分
     * @return 税込みの支払額（円）
     */
    public int totalWithTax(Order order, CustomerRank rank) {
        // 割引を反映した、税抜きの金額を求める
        int discounted = discountedSubtotal(order.subtotalBeforeTax(), rank);
        // ★重要★ 税込みであることを、メソッド名で伝える。呼び出し側が、もう一度税を足す誤りを防ぐ
        return discounted + discounted * TAX_PERCENT / 100;
    }

    /**
     * 顧客の区分に応じた割引を反映します。
     *
     * @param subtotalBeforeTax 税抜きの小計（円）
     * @param rank              顧客の区分
     * @return 割引を反映した、税抜きの金額（円）
     */
    private static int discountedSubtotal(int subtotalBeforeTax, CustomerRank rank) {
        // 会員でないとき
        if (rank != CustomerRank.MEMBER) {
            // 割引せずに返す
            return subtotalBeforeTax;
        }
        // 会員の割引率を引いた金額を返す
        return subtotalBeforeTax * (100 - MEMBER_DISCOUNT_PERCENT) / 100;
    }
}
