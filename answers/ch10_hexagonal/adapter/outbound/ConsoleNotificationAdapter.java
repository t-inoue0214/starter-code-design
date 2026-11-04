package com.example.orders.ch10_hexagonal.exercise.adapter.outbound;

import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.NotificationPort;
import java.io.PrintStream;

/**
 * 通知ポートの実装（アダプタ）です。コンソールに出力します。メール送信に替えるなら、このクラスの代わりを書くだけです。
 */
public class ConsoleNotificationAdapter implements NotificationPort {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public ConsoleNotificationAdapter(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 注文の成立を知らせます。
     *
     * @param order 注文
     */
    @Override
    public void orderPlaced(PlacedOrder order) {
        // ★重要★ 通知の手段（コンソールへの出力）は、このアダプタだけが知っている
        out.println("[NOTIFY] customer=" + order.customerId() + " order=" + order.orderId() + " total=" + order.total());
    }
}
