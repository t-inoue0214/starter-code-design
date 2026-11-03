package com.example.orders.ch12_event_driven.exercise.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 同じプロセスの中で、同期的にイベントを配るバスです。
 */
public class SimpleEventBus implements EventBus {
    private final Map<Class<?>, List<Consumer<Object>>> handlers = new HashMap<>();

    @Override
    public <E> void subscribe(Class<E> type, Consumer<E> handler) {
        // 型付きの処理を、Object を受ける処理にくるんで登録する
        handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(event -> handler.accept(type.cast(event)));
    }

    @Override
    public void publish(Object event) {
        // 登録順に呼ぶ。誰が登録されているかは、出す側は知らない
        for (Consumer<Object> handler : handlers.getOrDefault(event.getClass(), List.of())) {
            handler.accept(event);
        }
    }
}
