package com.example.orders.ch15_factory;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch15_factory.exercise.CouponRule;
import com.example.orders.ch15_factory.exercise.CouponRuleFactory;
import com.example.orders.ch15_factory.exercise.ExerciseOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;

/**
 * 「どのクーポンを使うか」の判断が工場に集まり、差し替えられることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CouponRuleFactoryTest {
    private final CouponRuleFactory factory = new CouponRuleFactory();

    /** WELCOME500 は500円引き、SAVE10 は10%引きのルールを返す。 */
    @Test
    @DisplayName("WELCOME500 は500円引き、SAVE10 は10%引きのルールを返す")
    void knownCoupons() {
        // 小計3000円に対して、WELCOME500 は500円、SAVE10 は300円を引く
        assertEquals(500, factory.create("WELCOME500").discountFor(3000));
        assertEquals(300, factory.create("SAVE10").discountFor(3000));
    }

    /** クーポンなし（null）は、割引0のルールを返す。 */
    @Test
    @DisplayName("クーポンなし（null）は、割引0のルールを返す")
    void noCoupon() {
        // クーポンを使わない注文は、null のまま渡される。エラーにせず、割引0として扱う
        assertEquals(0, factory.create(null).discountFor(3000));
    }

    /** 未知のコードは検証エラー。 */
    @Test
    @DisplayName("未知のコードは検証エラー")
    void unknownCoupon() {
        // 登録されていないコードは、検証エラーにする
        assertThrows(OrderValidationException.class, () -> factory.create("NOPE"));
    }

    /** 注文処理は、渡された工場が作るルールを使う（新しいクーポンを本体の変更なしで足せる）。 */
    @Test
    @DisplayName("注文処理は、渡された工場が作るルールを使う（新しいクーポンを本体の変更なしで足せる）")
    void processorUsesGivenFactory() {
        // ★重要★ 標準の工場を継承し、VIP20（20%引き）だけ足した工場を渡す。本体を変えずに、新しいクーポンが使える
        CouponRuleFactory custom = new CouponRuleFactory() {
            @Override
            public CouponRule create(String code) {
                if ("VIP20".equals(code)) {
                    return subtotal -> subtotal * 20 / 100;
                }
                return super.create(code);
            }
        };
        Map<String, Integer> stock = new HashMap<>();
        stock.put("P1", 10);
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        Order order = new Order("O1", customer, List.of(new OrderLine("P1", 1000, 5)), "VIP20",
                LocalDate.of(2026, 5, 10));
        int discount = new ExerciseOrderProcessor(stock, new PrintStream(System.out), custom).process(order).discount();
        assertEquals(1000, discount);
    }
}
