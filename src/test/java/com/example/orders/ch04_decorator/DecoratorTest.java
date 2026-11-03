package com.example.orders.ch04_decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch04_decorator.exercise.ExerciseOrderProcessor;
import com.example.orders.ch04_decorator.exercise.LoggingOrderProcessor;
import com.example.orders.ch04_decorator.exercise.TimingOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
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

/**
 * ログと計測を、本体を書き換えずに重ねられることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class DecoratorTest {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    private String text() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static Order order() {
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, 2)), null, LocalDate.of(2026, 5, 10));
    }

    private static final OrderProcessor SUCCESS = o -> new OrderResult(o.id(), 2000, 0, 2000);
    private static final OrderProcessor FAILURE = o -> {
        throw new OrderValidationException("壊れた注文");
    };

    @Test
    @DisplayName("ログのデコレータは、開始と成功を出力して結果をそのまま返す")
    void loggingSuccess() {
        OrderResult r = new LoggingOrderProcessor(SUCCESS, out).process(order());
        assertEquals(2000, r.total());
        assertTrue(text().contains("[LOG] start order=O1"));
        assertTrue(text().contains("[LOG] end order=O1 total=2000"));
    }

    @Test
    @DisplayName("ログのデコレータは、失敗を出力して例外をそのまま投げ直す")
    void loggingFailure() {
        assertThrows(OrderValidationException.class, () -> new LoggingOrderProcessor(FAILURE, out).process(order()));
        assertTrue(text().contains("[LOG] failed order=O1 reason=壊れた注文"));
    }

    @Test
    @DisplayName("計測のデコレータは、偽の時計で決まった時間を出力する（失敗時も出力する）")
    void timing() {
        AtomicLong ticks = new AtomicLong();
        // 呼ばれるたびに 5ms 進む偽の時計
        TimingOrderProcessor ok = new TimingOrderProcessor(SUCCESS, out, () -> ticks.getAndAdd(5_000_000));
        ok.process(order());
        assertTrue(text().contains("[TIME] order=O1 elapsed=5ms"));

        TimingOrderProcessor ng = new TimingOrderProcessor(FAILURE, out, () -> ticks.getAndAdd(7_000_000));
        assertThrows(OrderValidationException.class, () -> ng.process(order()));
        assertTrue(text().contains("elapsed=7ms"));
    }

    @Test
    @DisplayName("デコレータは重ねられ、開始・終了・計測の順に出力される")
    void chain() {
        AtomicLong ticks = new AtomicLong();
        OrderProcessor chain = new TimingOrderProcessor(new LoggingOrderProcessor(SUCCESS, out), out,
                () -> ticks.getAndAdd(1_000_000));
        chain.process(order());
        String t = text();
        assertTrue(t.indexOf("[LOG] start") < t.indexOf("[LOG] end"));
        assertTrue(t.indexOf("[LOG] end") < t.indexOf("[TIME]"));
    }

    @Test
    @DisplayName("本体にはログや計測が残っていない（デコレータと二重にならない）")
    void coreHasNoCrossCuttingCode() {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("P1", 10);
        new ExerciseOrderProcessor(stock, out).process(order());
        assertFalse(text().contains("[LOG]"));
        assertFalse(text().contains("[TIME]"));
    }
}
