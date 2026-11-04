package com.example.orders.ch07_event_driven;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch07_event_driven.exercise.ExerciseOrderApp;
import com.example.orders.ch07_event_driven.exercise.event.EventBus;
import com.example.orders.ch07_event_driven.exercise.event.OrderPlacedEvent;

/**
 * 既存のコードを書き換えずに、新しい受け取り手を足せることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class EventBusTest {
    /** テスト用の、最小のイベントバスです。 */
    private static class TestBus implements EventBus {
        // イベントの受け取り手の表を持つフィールド
        private final Map<Class<?>, List<Consumer<Object>>> handlers = new HashMap<>();
        // 出されたイベントの記録を持つフィールド
        private final List<Object> published = new ArrayList<>();

        /**
         * イベントの型ごとに、受け取り手を登録します。
         *
         * @param type    イベントの型
         * @param handler イベントを受け取る処理
         */
        @Override
        public <E> void subscribe(Class<E> type, Consumer<E> handler) {
            // イベントの型ごとの一覧に、受け取り手を追加する（なければ一覧を作る）
            handlers.computeIfAbsent(type, k -> new ArrayList<>()).add(e -> handler.accept(type.cast(e)));
        }

        /**
         * イベントを、登録された受け取り手へ配ります。
         *
         * @param event イベント
         */
        @Override
        public void publish(Object event) {
            // 出されたイベントを、記録しておく
            published.add(event);
            // このイベントの型に登録された受け取り手を、1つずつ取り出す
            for (Consumer<Object> h : handlers.getOrDefault(event.getClass(), List.of())) {
                // 受け取り手に、イベントを渡して処理させる
                h.accept(event);
            }
        }
    }

    // 出力の記録先を持つフィールド
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    // イベントバスを持つフィールド
    private TestBus bus;
    // テスト対象の注文アプリを持つフィールド
    private ExerciseOrderApp app;

    /** 各テストの前に、テスト用のバスとアプリを作り直します。 */
    @BeforeEach
    void setUp() {
        // ★重要★ 本物のバスの代わりに、出されたイベントを記録するテスト用のバスをアプリに渡す
        Map<String, Integer> stock = new HashMap<>();
        // 在庫に、P1を100個登録する
        stock.put("P1", 100);
        // 出されたイベントを記録するバスを作る
        bus = new TestBus();
        // ★重要★ アプリに、テスト用のバスを注入して組み立てる（バスは外から差し替えられる）
        app = new ExerciseOrderApp(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8),
                LocalDate.of(2026, 5, 10), bus);
    }

    /** 注文が成立すると、イベントが1つ出る。後から足した受け取り手も、それを受け取れる。 */
    @Test
    @DisplayName("注文が成立すると、イベントが1つ出る。後から足した受け取り手も、それを受け取れる")
    void newSubscriberReceivesEvent() {
        // 受け取ったイベントを入れる一覧を作る
        List<OrderPlacedEvent> received = new ArrayList<>();
        // ★重要★ アプリのコードを一切変えずに、受け取り手を足す
        bus.subscribe(OrderPlacedEvent.class, received::add);

        // コマンド「PLACE O1 C1 GOLD P1:1000:10」を実行する
        app.handle("PLACE O1 C1 GOLD P1:1000:10");

        // 件数が 1 になることを確かめる
        assertEquals(1, received.size());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("O1", received.get(0).orderId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("C1", received.get(0).customerId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(9000, received.get(0).total());
    }

    /** 失敗した注文では、イベントは出ない。 */
    @Test
    @DisplayName("失敗した注文では、イベントは出ない")
    void noEventOnFailure() {
        // 在庫100個に101個の注文は失敗する。成立していない注文のイベントは出ない
        app.handle("PLACE O1 C1 REGULAR P1:1000:101");
        // 条件が成り立つことを確かめる
        assertTrue(bus.published.isEmpty());
    }

    /** 通知と参照データの更新は、バスに登録された受け取り手が担う。 */
    @Test
    @DisplayName("通知と参照データの更新は、バスに登録された受け取り手が担う")
    void builtInSubscribersWorkThroughBus() {
        // アプリ内蔵の受け取り手（通知と参照データの更新）が、バスに登録されて動いている
        app.handle("PLACE O1 C1 GOLD P1:1000:10");
        // 条件が成り立つことを確かめる
        assertTrue(buffer.toString(StandardCharsets.UTF_8).contains("[NOTIFY] customer=C1 order=O1 total=9000"));
        // SHOW で、注文の内容が応答に出ることを確かめる
        assertEquals("ORDER O1 customer=C1 total=9000", app.handle("SHOW O1"));
    }
}
