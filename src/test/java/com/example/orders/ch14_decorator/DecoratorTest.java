package com.example.orders.ch14_decorator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch14_decorator.exercise.ExerciseOrderProcessor;
import com.example.orders.ch14_decorator.exercise.LoggingOrderProcessor;
import com.example.orders.ch14_decorator.exercise.TimingOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;

/**
 * ログと計測を、本体を書き換えずに重ねられることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class DecoratorTest {
    // 出力の記録先を持つフィールド
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    // 出力先を持つフィールド
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    /**
     * これまでの出力を、文字列で返します。
     *
     * @return 出力の内容
     */
    private String text() {
        // 記録した出力を、文字列にして返す
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * テスト用の注文を作ります。
     *
     * @return 注文
     */
    private static Order order() {
        // 連絡先のない一般会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        // テスト用の注文（P1を1000円で2個）を作って返す
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, 2)), null, LocalDate.of(2026, 5, 10));
    }

    // 常に成功する本体の偽物（金額は2000円）
    private static final OrderProcessor SUCCESS = o -> new OrderResult(o.id(), 2000, 0, 2000);
    // 常に検証エラーになる本体の偽物
    private static final OrderProcessor FAILURE = o -> {
        // 常に検証エラーを投げる
        throw new OrderValidationException("壊れた注文");
    };

    /** ログのデコレータは、開始と成功を出力して結果をそのまま返す。 */
    @Test
    @DisplayName("ログのデコレータは、開始と成功を出力して結果をそのまま返す")
    void loggingSuccess() {
        // 本体（常に成功する偽物）を包むと、結果は変えずに、開始と終了のログだけが増える
        OrderResult r = new LoggingOrderProcessor(SUCCESS, out).process(order());
        // 請求額が 2000 になることを確かめる
        assertEquals(2000, r.total());
        // 出力に「[LOG] start order=O1」が含まれることを確かめる
        assertTrue(text().contains("[LOG] start order=O1"));
        // 出力に「[LOG] end order=O1 total=2000」が含まれることを確かめる
        assertTrue(text().contains("[LOG] end order=O1 total=2000"));
    }

    /** ログのデコレータは、失敗を出力して例外をそのまま投げ直す。 */
    @Test
    @DisplayName("ログのデコレータは、失敗を出力して例外をそのまま投げ直す")
    void loggingFailure() {
        // 本体が失敗したら、失敗をログに残し、例外はそのまま呼び出し元へ投げ直す
        assertThrows(OrderValidationException.class, () -> new LoggingOrderProcessor(FAILURE, out).process(order()));
        // 出力に「[LOG] failed order=O1 reason=壊れた注文」が含まれることを確かめる
        assertTrue(text().contains("[LOG] failed order=O1 reason=壊れた注文"));
    }

    /** 計測のデコレータは、偽の時計で決まった時間を出力する（失敗時も出力する）。 */
    @Test
    @DisplayName("計測のデコレータは、偽の時計で決まった時間を出力する（失敗時も出力する）")
    void timing() {
        // 偽の時計の針を、0から始める
        AtomicLong ticks = new AtomicLong();
        // 呼ばれるたびに 5ms 進む偽の時計
        TimingOrderProcessor ok = new TimingOrderProcessor(SUCCESS, out, () -> ticks.getAndAdd(5_000_000));
        // 計測で包んだ処理で、注文を処理する
        ok.process(order());
        // 出力に「[TIME] order=O1 elapsed=5ms」が含まれることを確かめる
        assertTrue(text().contains("[TIME] order=O1 elapsed=5ms"));

        // 失敗する本体を、計測で包む（時計は7msずつ進む）
        TimingOrderProcessor ng = new TimingOrderProcessor(FAILURE, out, () -> ticks.getAndAdd(7_000_000));
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> ng.process(order()));
        // 出力に「elapsed=7ms」が含まれることを確かめる
        assertTrue(text().contains("elapsed=7ms"));
    }

    /** デコレータは重ねられ、開始・終了・計測の順に出力される。 */
    @Test
    @DisplayName("デコレータは重ねられ、開始・終了・計測の順に出力される")
    void chain() {
        // 計測の内側にログを重ねる。出力の順番は「開始ログ → 終了ログ → 計測」になる
        AtomicLong ticks = new AtomicLong();
        // ★重要★ 本体をログで包み、さらに計測で包む（デコレータを重ねる）
        OrderProcessor chain = new TimingOrderProcessor(new LoggingOrderProcessor(SUCCESS, out), out,
                () -> ticks.getAndAdd(1_000_000));
        // 重ねた処理で、注文を処理する
        chain.process(order());
        // これまでの出力を、文字列として取り出す
        String t = text();
        // 条件が成り立つことを確かめる
        assertTrue(t.indexOf("[LOG] start") < t.indexOf("[LOG] end"));
        // 条件が成り立つことを確かめる
        assertTrue(t.indexOf("[LOG] end") < t.indexOf("[TIME]"));
    }

    /** 本体にはログや計測が残っていない（デコレータと二重にならない）。 */
    @Test
    @DisplayName("本体にはログや計測が残っていない（デコレータと二重にならない）")
    void coreHasNoCrossCuttingCode() {
        // ★重要★ デコレータを使わない本体は、ログも計測も出力しない（二重にならない）
        Map<String, Integer> stock = new HashMap<>();
        // 在庫に、P1を10個登録する
        stock.put("P1", 10);
        // デコレータなしの本体で、注文を処理する
        new ExerciseOrderProcessor(stock, out).process(order());
        // 出力に「[LOG]」が含まれないことを確かめる
        assertFalse(text().contains("[LOG]"));
        // 出力に「[TIME]」が含まれないことを確かめる
        assertFalse(text().contains("[TIME]"));
    }
}
