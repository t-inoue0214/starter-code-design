package com.example.orders.ch04_layered.exercise.service;

import com.example.orders.domain.Order;
import java.io.PrintStream;

/**
 * 注文成立を通知します。コンソールに出力します。
 */
public class OrderNotifier {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public OrderNotifier(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * @param order 成立した注文
     * @param total 請求額（円）
     */
    public void orderPlaced(Order order, int total) {
        // 成立した注文を、通知として出力する
        out.println("[NOTIFY] customer=" + order.customer().id() + " order=" + order.id() + " total=" + total);
    }
}
