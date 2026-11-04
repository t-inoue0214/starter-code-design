package com.example.orders.ch07_event_driven.exercise.subscriber;

import com.example.orders.ch07_event_driven.exercise.event.EventBus;
import com.example.orders.ch07_event_driven.exercise.event.OrderPlacedEvent;
import com.example.orders.ch07_event_driven.exercise.query.OrderReadModel;

/**
 * 注文成立のイベントを受け取り、参照用の読み取りモデルを更新します。
 */
public class ReadModelSubscriber {
    // 読み取りモデルを持つフィールド
    private final OrderReadModel readModel;

    /**
     * @param readModel 更新する読み取りモデル
     */
    public ReadModelSubscriber(OrderReadModel readModel) {
        // 引数の読み取りモデルを、フィールドに保存する
        this.readModel = readModel;
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
        // 受け取ったイベントの内容を、読み取りモデルに反映する
        readModel.apply(event.orderId(), event.customerId(), event.total());
    }
}
