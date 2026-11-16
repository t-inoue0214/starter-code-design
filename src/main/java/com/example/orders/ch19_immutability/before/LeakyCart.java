package com.example.orders.ch19_immutability.before;

import com.example.orders.ch19_immutability.CartLine;
import java.util.List;

/**
 * イミュータブルに見えて、中身が漏れているカートです。フィールドは private final ですが、
 * 受け取った List をそのまま持ち、そのまま返すため、外から中身を変えられます。
 */
public class LeakyCart {
    // 行の一覧を持つフィールド（受け取った List そのもの）
    private final List<CartLine> lines;

    /**
     * カートを作ります。
     *
     * @param lines 行の一覧
     */
    public LeakyCart(List<CartLine> lines) {
        // 受け取った List を、そのまま保存する（呼び出し側と同じ List を共有してしまう）
        this.lines = lines;
    }

    /** @return 行の一覧（内部の List そのもの） */
    public List<CartLine> lines() {
        // 内部の List を、そのまま返す（受け取った側が変更できてしまう）
        return lines;
    }
}
