package com.example.orders.ch11_cqrs;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch11_cqrs.exercise.query.OrderQueryHandler;
import com.example.orders.ch11_cqrs.exercise.query.OrderReadModel;
import com.example.orders.ch11_cqrs.exercise.query.OrderSummary;
import com.example.orders.domain.OrderValidationException;

/**
 * 参照側が、更新側なしで、読み取りモデルだけを使って動くことを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class QuerySideTest {
    /** 読み取りモデルに反映するまでは見つからず、反映すると参照できる。 */
    @Test
    @DisplayName("読み取りモデルに反映するまでは見つからず、反映すると参照できる")
    void visibleAfterApply() {
        // ★重要★ 更新側からの反映（apply）がない間は、参照側から注文は見えない
        OrderReadModel model = new OrderReadModel();
        // 読み取りモデルを使う参照側を作る
        OrderQueryHandler queries = new OrderQueryHandler(model);
        // null であることを確かめる
        assertNull(model.summary("O1"));
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> queries.show("O1"));

        // 読み取りモデルに、O1（顧客C1、9000円）を反映する
        model.apply("O1", "C1", 9000);
        // 参照側から、O1の概要を取り出す
        OrderSummary s = queries.show("O1");
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("C1", s.customerId());
        // 請求額が 9000 になることを確かめる
        assertEquals(9000, s.total());
    }

    /** 顧客ごとの履歴は、反映した順に返り、他の顧客を含まない。 */
    @Test
    @DisplayName("顧客ごとの履歴は、反映した順に返り、他の顧客を含まない")
    void historyPerCustomer() {
        // C1 の注文が2件（O1、O3）、C2 の注文が1件（O2）。履歴は顧客ごとに、反映した順で返る
        OrderReadModel model = new OrderReadModel();
        // 読み取りモデルに、O1（顧客C1、100円）を反映する
        model.apply("O1", "C1", 100);
        // 読み取りモデルに、O2（顧客C2、200円）を反映する
        model.apply("O2", "C2", 200);
        // 読み取りモデルに、O3（顧客C1、300円）を反映する
        model.apply("O3", "C1", 300);
        // 読み取りモデルを使う参照側を作る
        OrderQueryHandler queries = new OrderQueryHandler(model);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of("O1", "O3"), queries.history("C1"));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of(), queries.history("C9"));
    }
}
