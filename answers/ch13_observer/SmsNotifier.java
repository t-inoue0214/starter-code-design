package com.example.orders.ch13_observer.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文成立をSMSで通知します（電話番号があり、請求額が1万円以上の場合だけ）。
 */
public class SmsNotifier implements OrderPlacedListener {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public SmsNotifier(PrintStream out) {
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
        // 電話番号がない、または1万円未満なら何もしない
        if (order.customer().phone() == null || result.total() < 10000) {
            return;
        }
        out.println("[SMS] to=" + order.customer().phone()
                + " order=" + order.id());
    }
}
