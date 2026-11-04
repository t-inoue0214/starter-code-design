package com.example.orders.ch07_event_driven.exercise.subscriber;

import com.example.orders.ch07_event_driven.exercise.event.EventBus;
import com.example.orders.ch07_event_driven.exercise.event.OrderPlacedEvent;
import java.io.PrintStream;

/**
 * 注文成立のイベントを受け取り、コンソールに通知を出力します。
 */
public class NotificationSubscriber {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public NotificationSubscriber(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 自分が関心のあるイベントを、バスに登録します。
     *
     * @param bus イベントバス
     */
    public void register(EventBus bus) {
        // 注文成立のイベントを受け取る処理として、自分の on メソッドを登録する
        bus.subscribe(OrderPlacedEvent.class, this::on);
    }

    /**
     * イベントを受け取って処理します。
     *
     * @param event イベント
     */
    private void on(OrderPlacedEvent event) {
        // 受け取ったイベントの内容を、通知として出力する
        out.println("[NOTIFY] customer=" + event.customerId() + " order=" + event.orderId() + " total=" + event.total());
    }
}
