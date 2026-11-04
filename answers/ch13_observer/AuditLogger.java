package com.example.orders.ch13_observer.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文成立を監査ログに残します。
 */
public class AuditLogger implements OrderPlacedListener {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public AuditLogger(PrintStream out) {
        this.out = out;
    }

    /**
     * 注文の成立を受け取って処理します。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void onOrderPlaced(Order order, OrderResult result) {
        // 誰の、どの注文が、いくらで成立したかを記録する
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + result.total());
    }
}
