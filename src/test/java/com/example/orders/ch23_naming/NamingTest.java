package com.example.orders.ch23_naming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch23_naming.before.OrderData;
import com.example.orders.ch23_naming.before.OrderManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 名前を直しても、振る舞いが変わらないことを確かめるテストです。
 * before の名前の悪いクラスと、名前を直したクラスに、同じ入力を渡して比べます。
 * このテストは、最初から全て green です。
 */
class NamingTest {

    /** 支払額は、名前を直しても同じ。 */
    @Test
    @DisplayName("支払額は、before の process と、名前を直した totalWithTax で同じになる")
    void totalIsTheSameAfterRenaming() {
        // before のクラスを作る
        OrderManager manager = new OrderManager();
        // 名前を直したクラスを作る
        CheckoutCalculator calculator = new CheckoutCalculator();
        // 一般の顧客で比べる（process の true と false が何かは、呼び出し側から読めない）
        assertEquals(manager.process(new OrderData("P", 10_000, 0), false),
                calculator.totalWithTax(new Order(OrderStatus.PLACED, 10_000, 0), CustomerRank.REGULAR));
        // 会員で比べる
        assertEquals(manager.process(new OrderData("P", 10_000, 0), true),
                calculator.totalWithTax(new Order(OrderStatus.PLACED, 10_000, 0), CustomerRank.MEMBER));
    }

    /** 支払額の具体的な値。 */
    @Test
    @DisplayName("会員は5%引きのあとに税10%が足される")
    void memberDiscountThenTax() {
        // 名前を直したクラスを作る
        CheckoutCalculator calculator = new CheckoutCalculator();
        // 一般の顧客は、10000円に税を足して11000円
        assertEquals(11_000, calculator.totalWithTax(new Order(OrderStatus.PLACED, 10_000, 0), CustomerRank.REGULAR));
        // 会員は、9500円に税を足して10450円
        assertEquals(10_450, calculator.totalWithTax(new Order(OrderStatus.PLACED, 10_000, 0), CustomerRank.MEMBER));
    }

    /** 取り消せるかの答えは、名前を直しても同じ。 */
    @Test
    @DisplayName("取り消せるかは、before の check と、名前を直した isCancellable で同じになる")
    void cancellableIsTheSameAfterRenaming() {
        // before のクラスを作る
        OrderManager manager = new OrderManager();
        // 確定済みで7日目は、どちらも true
        assertEquals(manager.check(new OrderData("P", 1000, 7)), new Order(OrderStatus.PLACED, 1000, 7).isCancellable());
        // 確定済みで8日目は、どちらも false
        assertEquals(manager.check(new OrderData("P", 1000, 8)), new Order(OrderStatus.PLACED, 1000, 8).isCancellable());
        // 出荷済みは、どちらも false
        assertEquals(manager.check(new OrderData("S", 1000, 0)), new Order(OrderStatus.SHIPPED, 1000, 0).isCancellable());
    }

    /** 名前が、そのまま文として読める。 */
    @Test
    @DisplayName("isCancellable は、期限内の確定済みなら true、それ以外は false")
    void isCancellableReadsAsASentence() {
        // 確定済みで期限内なら、取り消せる
        assertTrue(new Order(OrderStatus.PLACED, 1000, 7).isCancellable());
        // 期限を過ぎたら、取り消せない
        assertFalse(new Order(OrderStatus.PLACED, 1000, 8).isCancellable());
        // 出荷済みなら、取り消せない
        assertFalse(new Order(OrderStatus.SHIPPED, 1000, 0).isCancellable());
    }
}
