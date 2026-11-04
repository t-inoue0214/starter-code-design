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
        // 連絡先のない一般会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, null, null);
        // テスト用の注文（P1を1000円で2個）を作って返す
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, 2)), null, LocalDate.of(2026, 5, 10));
    }

    /** 追加の処理を、既存クラスを変えずに部品として差し込める。 */
    @Test
    @DisplayName("追加の処理を、既存クラスを変えずに部品として差し込める")
    void followUpCanBePlugged() {
        // ★重要★ ラムダ式で作った部品を、既存のクラスを変えずに差し込める。部品は、結果の請求額を記録する
        Map<String, Integer> stock = new HashMap<>();
        // 在庫に、P1を10個登録する
        stock.put("P1", 10);
        // 記録した請求額を入れる一覧を作る
        List<Integer> totals = new ArrayList<>();
        // ラムダ式で「請求額を記録する」部品を作る
        OrderFollowUp recorder = (order, result) -> totals.add(result.total());
        // ★重要★ 追加の処理を、部品としてコンストラクタに渡して組み立てる
        ExerciseOrderProcessor processor = new ExerciseOrderProcessor(stock, new PrintStream(System.out),
                List.of(recorder));
        // 注文を処理する
        processor.process(order());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of(2000), totals);
    }

    /** 追加の処理は、渡した順に実行される。 */
    @Test
    @DisplayName("追加の処理は、渡した順に実行される")
    void followUpsRunInOrder() {
        // 部品は、リストに並べた順に呼ばれる
        Map<String, Integer> stock = new HashMap<>();
        // 在庫に、P1を10個登録する
        stock.put("P1", 10);
        // 呼ばれた順の記録を入れる一覧を作る
        List<String> calls = new ArrayList<>();
        // ラムダ式で「呼ばれたら first と記録する」部品を作る
        OrderFollowUp first = (order, result) -> calls.add("first");
        // ラムダ式で「呼ばれたら second と記録する」部品を作る
        OrderFollowUp second = (order, result) -> calls.add("second");
        // ★重要★ 2つの部品を並べた順に渡して、注文を処理する
        new ExerciseOrderProcessor(stock, new PrintStream(System.out), List.of(first, second)).process(order());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of("first", "second"), calls);
    }

    /** 継承で処理を足していた子クラスが残っていない。 */
    @Test
    @DisplayName("継承で処理を足していた子クラスが残っていない")
    void noInheritanceLeft() {
        // ★重要★ 継承（extends）の子クラスが残っていれば、検査が失敗する。使わなくなった子クラスは削除する
        SourceRules.assertPackageExists("ch08_composition/exercise");
        // ★重要★ 継承で処理を足す子クラスが、残っていないことを確かめる
        SourceRules.assertNoTextInPackage("ch08_composition/exercise", " extends ",
                "継承で処理を足す形をやめ、部品を持って委譲してください。使わなくなった子クラスは削除します");
    }
}
