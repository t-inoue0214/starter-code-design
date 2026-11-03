package com.example.orders.ch10_hexagonal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch10_hexagonal.exercise.core.OrderService;
import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.NotificationPort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.OrderStorePort;
import com.example.orders.ch10_hexagonal.exercise.core.port.outbound.StockPort;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 業務の中心を、コンソールもMapも使わず、偽物のポートだけで動かせることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CoreWithFakesTest {
    private final Map<String, Integer> stockMap = new HashMap<>();
    private final Map<String, PlacedOrder> saved = new HashMap<>();
    private final List<PlacedOrder> notified = new ArrayList<>();
    private OrderService core;

    @BeforeEach
    void setUp() {
        stockMap.put("P1", 10);
        // 偽の出口。中心は、これが本物かどうかを知らない
        StockPort stocks = new StockPort() {
            public int available(String productId) {
                return stockMap.getOrDefault(productId, 0);
            }

            public void change(String productId, int delta) {
                stockMap.put(productId, available(productId) + delta);
            }
        };
        OrderStorePort store = new OrderStorePort() {
            public boolean exists(String orderId) {
                return saved.containsKey(orderId);
            }

            public void save(PlacedOrder order) {
                saved.put(order.orderId(), order);
            }

            public PlacedOrder find(String orderId) {
                return saved.get(orderId);
            }

            public List<String> idsOf(String customerId) {
                List<String> ids = new ArrayList<>();
                for (PlacedOrder p : saved.values()) {
                    if (p.customerId().equals(customerId)) {
                        ids.add(p.orderId());
                    }
                }
                return ids;
            }
        };
        NotificationPort notification = notified::add;
        core = new OrderService(stocks, store, notification);
    }

    private static Order order(String id, int quantity) {
        Customer customer = new Customer("C1", "山田", MemberRank.GOLD, null, null);
        return new Order(id, customer, List.of(new OrderLine("P1", 1000, quantity)), null, LocalDate.of(2026, 5, 10));
    }

    @Test
    @DisplayName("偽のポートだけで注文でき、在庫・保存・通知がポート越しに行われる")
    void placeThroughPorts() {
        OrderResult r = core.place(order("O1", 5));
        assertEquals(4500, r.total());
        assertEquals(5, stockMap.get("P1"));
        assertTrue(saved.containsKey("O1"));
        assertEquals(1, notified.size());
        assertEquals(4500, notified.get(0).total());
    }

    @Test
    @DisplayName("失敗した注文は、保存も通知もされない")
    void failureHasNoSideEffects() {
        assertThrows(OutOfStockException.class, () -> core.place(order("O1", 11)));
        assertEquals(10, stockMap.get("P1"));
        assertTrue(saved.isEmpty());
        assertTrue(notified.isEmpty());
    }

    @Test
    @DisplayName("参照と履歴も、ポート越しに動く。存在しない注文は検証エラー")
    void findAndHistory() {
        core.place(order("O1", 1));
        assertEquals("C1", core.find("O1").customerId());
        assertEquals(List.of("O1"), core.history("C1"));
        assertThrows(OrderValidationException.class, () -> core.find("O9"));
    }
}
