package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * メールで知らせます。
 */
public class MailFollowUp implements OrderFollowUp {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public MailFollowUp(PrintStream out) {
        this.out = out;
    }

    /**
     * メールで知らせます。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void run(Order order, OrderResult result) {
        if (order.customer().email() != null) {
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + result.total());
        }
    }
}
