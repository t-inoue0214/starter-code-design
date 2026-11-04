package com.example.orders.ch08_composition;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch08_composition.exercise.ExerciseOrderProcessor;
import com.example.orders.ch08_composition.exercise.OrderFollowUp;
import com.example.orders.contract.SourceRules;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;

/**
 * 継承ではなく、部品を持って委譲する形になっているかを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CompositionStructureTest {
    /**
     * テスト用の注文を作ります。
     *
     * @return 注文
     */
    private static Order order() {
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, 2)), null, LocalDate.of(2026, 5, 10));
    }

    /** 追加の処理を、既存クラスを変えずに部品として差し込める。 */
    @Test
    @DisplayName("追加の処理を、既存クラスを変えずに部品として差し込める")
    void followUpCanBePlugged() {
        // ★重要★ ラムダ式で作った部品を、既存のクラスを変えずに差し込める。部品は、結果の請求額を記録する
        Map<String, Integer> stock = new HashMap<>();
        stock.put("P1", 10);
        List<Integer> totals = new ArrayList<>();
        OrderFollowUp recorder = (order, result) -> totals.add(result.total());
        ExerciseOrderProcessor processor = new ExerciseOrderProcessor(stock, new PrintStream(System.out),
                List.of(recorder));
        processor.process(order());
        assertEquals(List.of(2000), totals);
    }

    /** 追加の処理は、渡した順に実行される。 */
    @Test
    @DisplayName("追加の処理は、渡した順に実行される")
    void followUpsRunInOrder() {
        // 部品は、リストに並べた順に呼ばれる
        Map<String, Integer> stock = new HashMap<>();
        stock.put("P1", 10);
        List<String> calls = new ArrayList<>();
        OrderFollowUp first = (order, result) -> calls.add("first");
        OrderFollowUp second = (order, result) -> calls.add("second");
        new ExerciseOrderProcessor(stock, new PrintStream(System.out), List.of(first, second)).process(order());
        assertEquals(List.of("first", "second"), calls);
    }

    /** 継承で処理を足していた子クラスが残っていない。 */
    @Test
    @DisplayName("継承で処理を足していた子クラスが残っていない")
    void noInheritanceLeft() {
        // ★重要★ 継承（extends）の子クラスが残っていれば、検査が失敗する。使わなくなった子クラスは削除する
        SourceRules.assertPackageExists("ch08_composition/exercise");
        SourceRules.assertNoTextInPackage("ch08_composition/exercise", " extends ",
                "継承で処理を足す形をやめ、部品を持って委譲してください。使わなくなった子クラスは削除します");
    }
}
