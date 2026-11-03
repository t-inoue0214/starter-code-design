package com.example.orders.ch11_cqrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.orders.ch11_cqrs.exercise.query.OrderQueryHandler;
import com.example.orders.ch11_cqrs.exercise.query.OrderReadModel;
import com.example.orders.ch11_cqrs.exercise.query.OrderSummary;
import com.example.orders.domain.OrderValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 参照側が、更新側なしで、読み取りモデルだけを使って動くことを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class QuerySideTest {
    @Test
    @DisplayName("読み取りモデルに反映するまでは見つからず、反映すると参照できる")
    void visibleAfterApply() {
        OrderReadModel model = new OrderReadModel();
        OrderQueryHandler queries = new OrderQueryHandler(model);
        assertNull(model.summary("O1"));
        assertThrows(OrderValidationException.class, () -> queries.show("O1"));

        model.apply("O1", "C1", 9000);
        OrderSummary s = queries.show("O1");
        assertEquals("C1", s.customerId());
        assertEquals(9000, s.total());
    }

    @Test
    @DisplayName("顧客ごとの履歴は、反映した順に返り、他の顧客を含まない")
    void historyPerCustomer() {
        OrderReadModel model = new OrderReadModel();
        model.apply("O1", "C1", 100);
        model.apply("O2", "C2", 200);
        model.apply("O3", "C1", 300);
        OrderQueryHandler queries = new OrderQueryHandler(model);
        assertEquals(List.of("O1", "O3"), queries.history("C1"));
        assertEquals(List.of(), queries.history("C9"));
    }
}
