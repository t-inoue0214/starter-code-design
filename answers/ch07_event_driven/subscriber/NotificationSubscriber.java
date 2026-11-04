package com.example.orders.ch07_event_driven.exercise.subscriber;

import com.example.orders.ch07_event_driven.exercise.event.EventBus;
import com.example.orders.ch07_event_driven.exercise.event.OrderPlacedEvent;
import java.io.PrintStream;

/**
 * 注文成立のイベントを受け取り、コンソールに通知を出力します。
 */
public class NotificationSubscriber {
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public NotificationSubscriber(PrintStream out) {
        this.out = out;
    }

    /**
     * 自分が関心のあるイベントを、バスに登録します。
     *
     * @param bus イベントバス
     */
    public void register(EventBus bus) {
        bus.subscribe(OrderPlacedEvent.class, this::on);
    }

    /**
     * イベントを受け取って処理します。
     *
     * @param event イベント
     */
    private void on(OrderPlacedEvent event) {
        out.println("[NOTIFY] customer=" + event.customerId() + " order=" + event.orderId() + " total=" + event.total());
    }
}
