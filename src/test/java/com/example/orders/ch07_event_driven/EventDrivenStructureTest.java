package com.example.orders.ch07_event_driven;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * イベントを出す側と、受け取る側が、互いを知らないことを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * order（出す側） → event ← subscriber（受け取る側）
 * </pre>
 */
class EventDrivenStructureTest {
    // ROOTを持つフィールド
    private static final String ROOT = "ch07_event_driven/exercise/";

    /** 出す側（order）と受け取る側（subscriber）のパッケージがある。 */
    @Test
    @DisplayName("出す側（order）と受け取る側（subscriber）のパッケージがある")
    void packagesExist() {
        // order パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "order");
        // subscriber パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "subscriber");
        // event パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "event");
    }

    /** 出す側は、受け取る側も、参照側も知らない。 */
    @Test
    @DisplayName("出す側は、受け取る側も、参照側も知らない")
    void publisherKnowsNoSubscriber() {
        // ★重要★ order が「.exercise.subscriber.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "order", ".exercise.subscriber.");
        // ★重要★ order が「.exercise.query.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "order", ".exercise.query.");
    }

    /** 出す側は、通知などの出力（java.io）を持たない。 */
    @Test
    @DisplayName("出す側は、通知などの出力（java.io）を持たない")
    void publisherHasNoOutput() {
        // ★重要★ order が「java.io.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "order", "java.io.");
    }

    /** 受け取る側は、出す側を知らない（イベントだけを知る）。 */
    @Test
    @DisplayName("受け取る側は、出す側を知らない（イベントだけを知る）")
    void subscriberDoesNotKnowPublisher() {
        // ★重要★ subscriber が「.exercise.order.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "subscriber", ".exercise.order.");
        // ★重要★ subscriber が「.exercise.event.」を使う（依存する）ことを確かめる
        SourceRules.assertSomeFileImports(ROOT + "subscriber", ".exercise.event.");
    }
}
