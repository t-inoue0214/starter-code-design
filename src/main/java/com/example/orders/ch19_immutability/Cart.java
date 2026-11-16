package com.example.orders.ch19_immutability;

import java.util.List;

/**
 * イミュータブルなカートです。技法: コレクションの防御的コピー（受け取るときと、渡すとき）。
 * 比べるための before/LeakyCart は、受け取った List をそのまま持つため、外から中身を変えられます。
 */
public final class Cart {
    // 行の一覧を持つフィールド。変更できない List を持つ
    private final List<CartLine> lines;

    /**
     * カートを作ります。
     *
     * @param lines 行の一覧。呼び出し側があとで変更しても、このカートには影響しない
     */
    public Cart(List<CartLine> lines) {
        // ★重要★ 受け取った List を、変更できないコピーにして持つ（呼び出し側の List と切り離す）
        this.lines = List.copyOf(lines);
    }

    /** @return 行の一覧。変更しようとすると例外になる */
    public List<CartLine> lines() {
        // ★重要★ List.copyOf で作った List は変更できないので、そのまま渡しても中身を守れる
        return lines;
    }

    /** @return カートの合計金額 */
    public Money total() {
        // 合計を0円から始める
        Money total = Money.ZERO;
        // 行を1つずつ調べる
        for (CartLine line : lines) {
            // 行の金額を足した、新しい合計に置き換える（変数の置き換えで、オブジェクトは変えていない）
            total = total.plus(line.subtotal());
        }
        // 合計を返す
        return total;
    }
}
