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
        // 1000円に500円を足した、新しい金額を作る
        Money b = a.plus(Money.of(500));
        // 金額（円）が 1000 になることを確かめる
        assertEquals(1000, a.yen());
        // 金額（円）が 1500 になることを確かめる
        assertEquals(1500, b.yen());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.of(1500), b);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.of(1500).hashCode(), b.hashCode());
        // 実際の値が、期待値と異なることを確かめる
        assertNotEquals(Money.of(1), Money.of(2));
    }

    /** Money の計算（掛け算、割合、最小値、比較）。 */
    @Test
    @DisplayName("Money の計算（掛け算、割合、最小値、比較）")
    void moneyCalculations() {
        // percent は端数を切り捨てる（999円の3%は29円）
        assertEquals(Money.of(3000), Money.of(1000).times(3));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.of(100), Money.of(1000).percent(10));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.of(29), Money.of(999).percent(3));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.of(300), Money.of(300).min(Money.of(500)));
        // 条件が成り立つことを確かめる
        assertTrue(Money.of(2).isGreaterThan(Money.of(1)));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Money.zero(), Money.of(0));
    }

    /** Money は負の金額を作れない。 */
    @Test
    @DisplayName("Money は負の金額を作れない")
    void moneyRejectsNegative() {
        // 負の金額は、作るときも、引き算の結果でも作れない
        assertThrows(IllegalArgumentException.class, () -> Money.of(-1));
        // 例外が投げられることを確かめる
        assertThrows(IllegalArgumentException.class, () -> Money.of(100).minus(Money.of(101)));
    }

    /** Stock は、足りるかの判断を自分で行い、引き当てと戻しができる。 */
    @Test
    @DisplayName("Stock は、足りるかの判断を自分で行い、引き当てと戻しができる")
    void stockOwnsItsRule() {
        // 足りないときは例外になり、失敗した引き当ては在庫を減らさない。release で戻せる
        Stock stock = new Stock("P1", 5);
        // 3個を引き当てる
        stock.reserve(3);
        // 在庫が2個であることを確かめる
        assertEquals(2, stock.quantity());
        // 在庫(2個)を超える3個の引き当てが、在庫不足の例外になることを確かめる
        OutOfStockException e = assertThrows(OutOfStockException.class, () -> stock.reserve(3));
        // 条件が成り立つことを確かめる
        assertTrue(e.getMessage().contains("P1"));
        // 在庫が2個であることを確かめる
        assertEquals(2, stock.quantity());
        // 3個を戻す
        stock.release(3);
        // 在庫数が 5 になることを確かめる
        assertEquals(5, stock.quantity());
    }

    /** InMemoryStockRepository は、存在しない商品を在庫0で返し、保存でMapに書き戻す。 */
    @Test
    @DisplayName("InMemoryStockRepository は、存在しない商品を在庫0で返し、保存でMapに書き戻す")
    void inMemoryRepository() {
        // 存在しない商品は在庫0として返す。save すると、元のMapに書き戻される
        Map<String, Integer> map = new HashMap<>();
        // Mapに、P1を10個登録する
        map.put("P1", 10);
        // Mapを保存先にした、メモリ上の保存先を作る
        StockRepository repository = new InMemoryStockRepository(map);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(10, repository.find("P1").quantity());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(0, repository.find("NONE").quantity());
        // 保存先から、P1の在庫を取り出す
        Stock stock = repository.find("P1");
        // 4個を引き当てる
        stock.reserve(4);
        // 変更した在庫を、保存先に書き戻す
        repository.save(stock);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(6, map.get("P1"));
    }

    /** 注文処理は、渡された保存先（偽物）を使って在庫を引き当てる。 */
    @Test
    @DisplayName("注文処理は、渡された保存先（偽物）を使って在庫を引き当てる")
    void processorUsesGivenRepository() {
        // ★重要★ 保存先を偽物（保存したものを覚えておくだけ）に差し替えても、注文処理が動く
        Map<String, Stock> saved = new HashMap<>();
        // 保存したものを覚えておくだけの、偽の保存先を作る
        StockRepository fake = new StockRepository() {
            @Override
            // 保存済みの在庫があればそれを、なければ10個の在庫を返す
            public Stock find(String productId) {
                // 保存済みの在庫か、なければ新しい在庫を返す
                return saved.getOrDefault(productId, new Stock(productId, 10));
            }

            @Override
            // 渡された在庫を、覚えておく
            public void save(Stock stock) {
                // 商品IDに対応づけて、在庫を覚える
                saved.put(stock.productId(), stock);
            }
        };
        // 連絡先のない一般会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        // P1を100円で4個の注文を作る
        Order order = new Order("O1", customer, List.of(new OrderLine("P1", 100, 4)), null, LocalDate.of(2026, 5, 10));
        // ★重要★ 偽の保存先を渡して組み立てた注文処理で、注文を処理する
        new ExerciseOrderProcessor(fake, new PrintStream(System.out)).process(order);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(6, saved.get("P1").quantity());
    }
}
