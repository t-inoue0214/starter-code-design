package com.example.orders.ch07_event_driven.exercise.event;

import java.util.function.Consumer;

/**
 * イベントバスの窓口です。イベントを出す側と、受け取る側を、仲介します。
 * テストがこの形を前提にしているため、形は変えないでください。
 */
public interface EventBus {
    /**
     * 指定した種類のイベントを受け取る処理を登録します。
     *
     * @param type    イベントの種類
     * @param handler 受け取ったときの処理
     * @param <E>     イベントの型
     */
    <E> void subscribe(Class<E> type, Consumer<E> handler);

    /**
     * イベントを出します。登録された処理が、登録順に呼ばれます。
     *
     * @param event イベント
     */
    void publish(Object event);
}
