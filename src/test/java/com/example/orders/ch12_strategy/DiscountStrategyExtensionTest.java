package com.example.orders.ch12_strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.orders.ch12_strategy.exercise.DiscountStrategy;
import com.example.orders.ch12_strategy.exercise.ExerciseOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 割引ルールを「既存コードを書き換えずに」足せることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class DiscountStrategyExtensionTest {
    /**
     * テスト用の注文を作ります。
     *
     * @return 注文
     */
    private static Order order() {
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, 2)), null, LocalDate.of(2026, 5, 10));
    }

    /**
     * テスト対象の注文処理を、指定の部品で組み立てます。
     *
     * @param strategies 割引ルールの一覧
     * @return 注文処理
     */
    private static ExerciseOrderProcessor processor(List<DiscountStrategy> strategies) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("P1", 10);
        return new ExerciseOrderProcessor(stock, new PrintStream(System.out), strategies);
    }

    /** 法人向けの割引ルールを、既存クラスを変更せずに追加できる。 */
    @Test
    @DisplayName("法人向けの割引ルールを、既存クラスを変更せずに追加できる")
    void addCorporateDiscount() {
        // ラムダ式で「常に100円引き」のルールを作って渡す。小計2000円から100円が引かれる
        DiscountStrategy corporate = (order, subtotal) -> 100;
        OrderResult r = processor(List.of(corporate)).process(order());
        assertEquals(100, r.discount());
        assertEquals(1900, r.total());
    }

    /** 割引ルールは合算され、ルールが空なら割引なし。 */
    @Test
    @DisplayName("割引ルールは合算され、ルールが空なら割引なし")
    void rulesAreSummed() {
        // 複数のルールを渡すと、割引は合算される（100円 ＋ 小計の10%）。ルールが空なら割引0
        DiscountStrategy a = (order, subtotal) -> 100;
        DiscountStrategy b = (order, subtotal) -> subtotal / 10;
        assertEquals(300, processor(List.of(a, b)).process(order()).discount());
        assertEquals(0, processor(List.of()).process(order()).discount());
    }
}
