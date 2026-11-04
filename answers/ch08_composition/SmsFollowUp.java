package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * SMSで知らせます。
 */
public class SmsFollowUp implements OrderFollowUp {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public SmsFollowUp(PrintStream out) {
        this.out = out;
    }

    /**
     * SMSで知らせます。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void run(Order order, OrderResult result) {
        if (order.customer().phone() != null && result.total() >= 10000) {
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }
    }
}
