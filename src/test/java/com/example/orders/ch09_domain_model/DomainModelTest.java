package com.example.orders.ch09_domain_model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch09_domain_model.exercise.ExerciseOrderProcessor;
import com.example.orders.ch09_domain_model.exercise.InMemoryStockRepository;
import com.example.orders.ch09_domain_model.exercise.Money;
import com.example.orders.ch09_domain_model.exercise.Stock;
import com.example.orders.ch09_domain_model.exercise.StockRepository;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OutOfStockException;

/**
 * 業務ルールが Money と Stock に移り、保存先が差し替えられることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class DomainModelTest {
    /** Money は計算しても元の値を変えず、金額が同じなら等しい。 */
    @Test
    @DisplayName("Money は計算しても元の値を変えず、金額が同じなら等しい")
    void moneyIsImmutableValue() {
        // plus は新しい Money を返し、元の値（1000円）は変わらない。金額が同じなら等しく、ハッシュ値も同じ
        Money a = Money.of(1000);
        Money b = a.plus(Money.of(500));
        assertEquals(1000, a.yen());
        assertEquals(1500, b.yen());
        assertEquals(Money.of(1500), b);
        assertEquals(Money.of(1500).hashCode(), b.hashCode());
        assertNotEquals(Money.of(1), Money.of(2));
    }

    /** Money の計算（掛け算、割合、最小値、比較）。 */
    @Test
    @DisplayName("Money の計算（掛け算、割合、最小値、比較）")
    void moneyCalculations() {
        // percent は端数を切り捨てる（999円の3%は29円）
        assertEquals(Money.of(3000), Money.of(1000).times(3));
        assertEquals(Money.of(100), Money.of(1000).percent(10));
        assertEquals(Money.of(29), Money.of(999).percent(3));
        assertEquals(Money.of(300), Money.of(300).min(Money.of(500)));
        assertTrue(Money.of(2).isGreaterThan(Money.of(1)));
        assertEquals(Money.zero(), Money.of(0));
    }

    /** Money は負の金額を作れない。 */
    @Test
    @DisplayName("Money は負の金額を作れない")
    void moneyRejectsNegative() {
        // 負の金額は、作るときも、引き算の結果でも作れない
        assertThrows(IllegalArgumentException.class, () -> Money.of(-1));
        assertThrows(IllegalArgumentException.class, () -> Money.of(100).minus(Money.of(101)));
    }

    /** Stock は、足りるかの判断を自分で行い、引き当てと戻しができる。 */
    @Test
    @DisplayName("Stock は、足りるかの判断を自分で行い、引き当てと戻しができる")
    void stockOwnsItsRule() {
        // 足りないときは例外になり、失敗した引き当ては在庫を減らさない。release で戻せる
        Stock stock = new Stock("P1", 5);
        stock.reserve(3);
        assertEquals(2, stock.quantity());
        OutOfStockException e = assertThrows(OutOfStockException.class, () -> stock.reserve(3));
        assertTrue(e.getMessage().contains("P1"));
        assertEquals(2, stock.quantity());
        stock.release(3);
        assertEquals(5, stock.quantity());
    }

    /** InMemoryStockRepository は、存在しない商品を在庫0で返し、保存でMapに書き戻す。 */
    @Test
    @DisplayName("InMemoryStockRepository は、存在しない商品を在庫0で返し、保存でMapに書き戻す")
    void inMemoryRepository() {
        // 存在しない商品は在庫0として返す。save すると、元のMapに書き戻される
        Map<String, Integer> map = new HashMap<>();
        map.put("P1", 10);
        StockRepository repository = new InMemoryStockRepository(map);
        assertEquals(10, repository.find("P1").quantity());
        assertEquals(0, repository.find("NONE").quantity());
        Stock stock = repository.find("P1");
        stock.reserve(4);
        repository.save(stock);
        assertEquals(6, map.get("P1"));
    }

    /** 注文処理は、渡された保存先（偽物）を使って在庫を引き当てる。 */
    @Test
    @DisplayName("注文処理は、渡された保存先（偽物）を使って在庫を引き当てる")
    void processorUsesGivenRepository() {
        // ★重要★ 保存先を偽物（保存したものを覚えておくだけ）に差し替えても、注文処理が動く
        Map<String, Stock> saved = new HashMap<>();
        StockRepository fake = new StockRepository() {
            @Override
            public Stock find(String productId) {
                return saved.getOrDefault(productId, new Stock(productId, 10));
            }

            @Override
            public void save(Stock stock) {
                saved.put(stock.productId(), stock);
            }
        };
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        Order order = new Order("O1", customer, List.of(new OrderLine("P1", 100, 4)), null, LocalDate.of(2026, 5, 10));
        new ExerciseOrderProcessor(fake, new PrintStream(System.out)).process(order);
        assertEquals(6, saved.get("P1").quantity());
    }
}
