package com.example.orders.ch02_roles.exercise;

import com.example.orders.domain.Order;
import java.io.PrintStream;

/**
 * 注文の成立をコンソールへ知らせます。出力という技術的な処理を、業務ロジックから切り離すためのクラスです。
 */
public class ConsoleNotifier {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 通知の出力先
     */
    public ConsoleNotifier(PrintStream out) {
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
