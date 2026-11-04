package com.example.orders.ch10_hexagonal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

/**
 * 業務の中心を、コンソールもMapも使わず、偽物のポートだけで動かせることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CoreWithFakesTest {
    // stockMapを持つフィールド
    private final Map<String, Integer> stockMap = new HashMap<>();
    // 保存された注文を持つフィールド
    private final Map<String, PlacedOrder> saved = new HashMap<>();
    // notifiedを持つフィールド
    private final List<PlacedOrder> notified = new ArrayList<>();
    // テスト対象の中心を持つフィールド
    private OrderService core;

    /** 各テストの前に、偽のポートで中心を組み立てます。 */
    @BeforeEach
    void setUp() {
        // 在庫に、P1を10個登録する
        stockMap.put("P1", 10);
        // ★重要★ 偽の出口。中心は、これが本物かどうかを知らない
        StockPort stocks = new StockPort() {
            // 在庫数を返す
            public int available(String productId) {
                // 在庫数を返す（登録がなければ0）
                return stockMap.getOrDefault(productId, 0);
            }

            // 在庫を増減する
            public void change(String productId, int delta) {
                // 現在の在庫数に増減分を足す
                stockMap.put(productId, available(productId) + delta);
            }
        };
        // ★重要★ 保存先のポートの偽物を作る（中心は、これが本物かどうかを知らない）
        OrderStorePort store = new OrderStorePort() {
            // 保存済みかどうかを返す
            public boolean exists(String orderId) {
                // 保存済みかどうかを返す
                return saved.containsKey(orderId);
            }

            // 注文を保存する
            public void save(PlacedOrder order) {
                // 注文を、注文IDに対応づけて覚える
                saved.put(order.orderId(), order);
            }

            // 注文を取り出す
            public PlacedOrder find(String orderId) {
                // 保存済みの注文を返す
                return saved.get(orderId);
            }

            // 顧客の注文ID一覧を返す
            public List<String> idsOf(String customerId) {
                // 注文IDを入れる一覧を作る
                List<String> ids = new ArrayList<>();
                // 保存済みの注文を、1件ずつ取り出す
                for (PlacedOrder p : saved.values()) {
                    // 注文の顧客が、指定の顧客と同じとき
                    if (p.customerId().equals(customerId)) {
                        // その注文のIDを、一覧に加える
                        ids.add(p.orderId());
                    }
                }
                // 集めた注文ID一覧を返す
                return ids;
            }
        };
        // 通知は、受け取った注文を記録するだけの偽物
        NotificationPort notification = notified::add;
        // ★重要★ 偽のポートだけで、中心を組み立てる
        core = new OrderService(stocks, store, notification);
    }

    /**
     * テスト用の注文を作ります。
     *
     * @param id       注文ID
     * @param quantity 数量
     * @return 注文
     */
    private static Order order(String id, int quantity) {
        // 連絡先のないゴールド会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.GOLD, null, null);
        // P1を1000円で指定の数量だけ注文する、テスト用の注文を作る
        return new Order(id, customer, List.of(new OrderLine("P1", 1000, quantity)), null, LocalDate.of(2026, 5, 10));
    }

    /** 偽のポートだけで注文でき、在庫・保存・通知がポート越しに行われる。 */
    @Test
    @DisplayName("偽のポートだけで注文でき、在庫・保存・通知がポート越しに行われる")
    void placeThroughPorts() {
        // ゴールド会員の1000円 × 5個で、10%引きの4500円。在庫・保存・通知が、すべて偽のポートに届く
        OrderResult r = core.place(order("O1", 5));
        // 請求額が 4500 になることを確かめる
        assertEquals(4500, r.total());
        // P1の在庫が 5 個になることを確かめる
        assertEquals(5, stockMap.get("P1"));
        // 条件が成り立つことを確かめる
        assertTrue(saved.containsKey("O1"));
        // 件数が 1 になることを確かめる
        assertEquals(1, notified.size());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(4500, notified.get(0).total());
    }

    /** 失敗した注文は、保存も通知もされない。 */
    @Test
    @DisplayName("失敗した注文は、保存も通知もされない")
    void failureHasNoSideEffects() {
        // 在庫10個に11個の注文は失敗する。失敗した注文は、在庫を減らさず、保存も通知もしない
        assertThrows(OutOfStockException.class, () -> core.place(order("O1", 11)));
        // P1の在庫が 10 個になることを確かめる
        assertEquals(10, stockMap.get("P1"));
        // 条件が成り立つことを確かめる
        assertTrue(saved.isEmpty());
        // 条件が成り立つことを確かめる
        assertTrue(notified.isEmpty());
    }

    /** 参照と履歴も、ポート越しに動く。存在しない注文は検証エラー。 */
    @Test
    @DisplayName("参照と履歴も、ポート越しに動く。存在しない注文は検証エラー")
    void findAndHistory() {
        // 注文を1件通したあとで、参照と履歴が使える。存在しない注文は検証エラー
        core.place(order("O1", 1));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("C1", core.find("O1").customerId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of("O1"), core.history("C1"));
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> core.find("O9"));
    }
}
