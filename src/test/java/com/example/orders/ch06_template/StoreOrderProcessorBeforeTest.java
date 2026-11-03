package com.example.orders.ch06_template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch06_template.before.StoreOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 第6章の分解前の店頭注文の仕様を確かめます。
 */
class StoreOrderProcessorBeforeTest {
    private Map<String, Integer> stock;
    private ByteArrayOutputStream buffer;
    private StoreOrderProcessor processor;

    @BeforeEach
    void setUp() {
        stock = new HashMap<>();
        stock.put("P1", 100);
        buffer = new ByteArrayOutputStream();
        processor = new StoreOrderProcessor(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private Order order(int unitPrice, int quantity) {
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, "a@example.com", "090");
        return new Order("O1", customer, List.of(new OrderLine("P1", unitPrice, quantity)), null, LocalDate.of(2026, 5, 10));
    }

    private String text() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("店頭は50万円を超えても、100万円までなら決済できる")
    void higherLimit() {
        OrderResult r = processor.process(order(100000, 6));
        assertEquals(600000, r.total());
    }

    @Test
    @DisplayName("店頭でも、100万円を超えると決済拒否になり、在庫が戻る")
    void declinedOverLimit() {
        assertThrows(PaymentDeclinedException.class, () -> processor.process(order(100000, 11)));
        assertEquals(100, stock.get("P1"));
    }

    @Test
    @DisplayName("店頭ではメールもSMSも送らず、監査ログに店頭の印が付く")
    void noNotificationAndStoreAudit() {
        processor.process(order(5000, 2));
        assertFalse(text().contains("[MAIL]"));
        assertFalse(text().contains("[SMS]"));
        assertTrue(text().contains("[PAYMENT] order=O1 amount=10000"));
        assertTrue(text().contains("[AUDIT] order=O1 customer=C1 total=10000 channel=STORE"));
    }
}
