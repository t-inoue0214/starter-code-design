package com.example.orders.ch03_observer.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文成立をメールで通知します（メールアドレスがある場合だけ）。
 */
public class MailNotifier implements OrderPlacedListener {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public MailNotifier(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onOrderPlaced(Order order, OrderResult result) {
        // メールアドレスがなければ何もしない
        if (order.customer().email() == null) {
            return;
        }
        out.println("[MAIL] to=" + order.customer().email()
                + " order=" + order.id() + " total=" + result.total());
    }
}
