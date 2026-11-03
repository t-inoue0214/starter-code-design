package com.example.orders.ch08_layered.exercise.service;

import com.example.orders.domain.Order;
import java.io.PrintStream;

/**
 * 注文成立を通知します。コンソールに出力します。
 */
public class OrderNotifier {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public OrderNotifier(PrintStream out) {
        this.out = out;
    }

    /**
     * @param order 成立した注文
     * @param total 請求額（円）
     */
    public void orderPlaced(Order order, int total) {
        out.println("[NOTIFY] customer=" + order.customer().id() + " order=" + order.id() + " total=" + total);
    }
}
