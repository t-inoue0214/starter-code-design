package com.example.orders.ch10_hexagonal.exercise.adapter.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.NotificationPort;
import java.io.PrintStream;

/**
 * 通知ポートの実装（アダプタ）です。コンソールに出力します。メール送信に替えるなら、このクラスの代わりを書くだけです。
 */
public class ConsoleNotificationAdapter implements NotificationPort {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public ConsoleNotificationAdapter(PrintStream out) {
        this.out = out;
    }

    @Override
    public void orderPlaced(PlacedOrder order) {
        out.println("[NOTIFY] customer=" + order.customerId() + " order=" + order.orderId() + " total=" + order.total());
    }
}
