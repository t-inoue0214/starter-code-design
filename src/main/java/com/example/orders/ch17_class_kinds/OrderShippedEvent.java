package com.example.orders.ch17_class_kinds;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * イベント（メッセージ）の例です。「注文が出荷された」という、起きた事実を表す不変のデータです。
 * 名前は過去形にし、起きた事実だけを持たせます。
 * 重複を見分けるためのイベントID、いつ起きたか、誰の要求で起きたかも持たせます。
 * 新規に作るときと、保存済みのものを復元するときで、作り方を分けています。
 */
public final class OrderShippedEvent {
    // eventIdを持つフィールド
    private final String eventId;
    // occurredAtを持つフィールド
    private final Instant occurredAt;
    // requestedByを持つフィールド
    private final String requestedBy;
    // 注文IDを持つフィールド
    private final String orderId;
    // shippedOnを持つフィールド
    private final LocalDate shippedOn;

    /**
     * 値をそのまま保存します。外からは、新規作成の occur か、復元の restore を使います。
     *
     * @param eventId     イベントID
     * @param occurredAt  発生時刻
     * @param requestedBy 要求元
     * @param orderId     出荷された注文のID
     * @param shippedOn   出荷した日
     */
    private OrderShippedEvent(String eventId, Instant occurredAt, String requestedBy,
            String orderId, LocalDate shippedOn) {
        // 引数のeventIdを、フィールドに保存する
        this.eventId = eventId;
        // 引数のoccurredAtを、フィールドに保存する
        this.occurredAt = occurredAt;
        // 引数のrequestedByを、フィールドに保存する
        this.requestedBy = requestedBy;
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数のshippedOnを、フィールドに保存する
        this.shippedOn = shippedOn;
    }

    /**
     * 新しく起きたイベントを作ります。イベントIDと発生時刻は、ここで決まります。
     * テストで固定できるよう、ID の生成と時計を、外から渡せます。
     *
     * @param orderId     出荷された注文のID
     * @param shippedOn   出荷した日
     * @param requestedBy 出荷を要求した人（または仕組み）
     * @param idGenerator イベントIDの生成方法
     * @param clock       発生時刻を決める時計
     * @return 新しいイベント
     */
    public static OrderShippedEvent occur(String orderId, LocalDate shippedOn, String requestedBy,
            Supplier<String> idGenerator, Clock clock) {
        // ★重要★ イベントIDと発生時刻は、新しく作るこの場面でだけ決める
        return new OrderShippedEvent(idGenerator.get(), clock.instant(), requestedBy, orderId, shippedOn);
    }

    /**
     * 標準の方法（UUID と現在時刻）で、新しく起きたイベントを作ります。
     *
     * @param orderId     出荷された注文のID
     * @param shippedOn   出荷した日
     * @param requestedBy 出荷を要求した人（または仕組み）
     * @return 新しいイベント
     */
    public static OrderShippedEvent occur(String orderId, LocalDate shippedOn, String requestedBy) {
        // 標準では UUID と現在時刻を使う（テストでは、上の方法で差し替えられる）
        return occur(orderId, shippedOn, requestedBy, () -> UUID.randomUUID().toString(), Clock.systemUTC());
    }

    /**
     * 保存済みのイベントを復元します。イベントIDと発生時刻は、保存されていた値をそのまま使います。
     *
     * @param eventId     保存されていたイベントID
     * @param occurredAt  保存されていた発生時刻
     * @param requestedBy 保存されていた要求元
     * @param orderId     出荷された注文のID
     * @param shippedOn   出荷した日
     * @return 復元したイベント
     */
    public static OrderShippedEvent restore(String eventId, Instant occurredAt, String requestedBy,
            String orderId, LocalDate shippedOn) {
        // ★重要★ 復元では、保存されていたIDと時刻をそのまま使う（新しく生成しない）
        return new OrderShippedEvent(eventId, occurredAt, requestedBy, orderId, shippedOn);
    }

    /** @return イベントID（重複の見分けに使う） */
    public String eventId() { return eventId; }

    /** @return 発生時刻 */
    public Instant occurredAt() { return occurredAt; }

    /** @return 要求元 */
    public String requestedBy() { return requestedBy; }

    /** @return 出荷された注文のID */
    public String orderId() { return orderId; }

    /** @return 出荷した日 */
    public LocalDate shippedOn() { return shippedOn; }

    /**
     * イベントIDが同じなら、同じイベントとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // ★重要★ イベントIDが同じなら同じイベント（重複して届いても見分けられる）
        return o instanceof OrderShippedEvent && ((OrderShippedEvent) o).eventId.equals(eventId);
    }

    /** @return イベントIDから計算したハッシュ値 */
    @Override
    public int hashCode() {
        // イベントIDから計算したハッシュ値を返す（equals と整合させる）
        return eventId.hashCode();
    }
}
