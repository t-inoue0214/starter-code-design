package com.example.orders.ch12_event_driven.exercise.subscriber;

import com.example.orders.ch12_event_driven.exercise.event.EventBus;
import com.example.orders.ch12_event_driven.exercise.event.OrderPlacedEvent;
import com.example.orders.ch12_event_driven.exercise.query.OrderReadModel;

/**
 * 注文成立のイベントを受け取り、参照用の読み取りモデルを更新します。
 */
public class ReadModelSubscriber {
    private final OrderReadModel readModel;

    /**
     * @param readModel 更新する読み取りモデル
     */
    public ReadModelSubscriber(OrderReadModel readModel) {
        this.readModel = readModel;
    }

    /**
     * 自分が関心のあるイベントを、バスに登録します。
     *
     * @param bus イベントバス
     */
    public void register(EventBus bus) {
        bus.subscribe(OrderPlacedEvent.class, this::on);
    }

    private void on(OrderPlacedEvent event) {
        readModel.apply(event.orderId(), event.customerId(), event.total());
    }
}
