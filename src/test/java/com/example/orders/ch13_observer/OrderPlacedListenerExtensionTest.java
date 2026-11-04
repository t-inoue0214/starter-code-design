package com.example.orders.ch13_observer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.orders.ch13_observer.exercise.ExerciseOrderProcessor;
import com.example.orders.ch13_observer.exercise.OrderPlacedListener;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OutOfStockException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 通知先を「既存コードを書き換えずに」足せることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class OrderPlacedListenerExtensionTest {
    // 出力の記録先を持つフィールド
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    // 出力先を持つフィールド
    private final PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

    /**
     * テスト用の注文を作ります。
     *
     * @param quantity 数量
     * @return 注文
     */
    private static Order order(int quantity) {
        // メールアドレスと電話番号のある一般会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, "a@example.com", "090");
        // P1を1000円で指定の数量だけ注文する、テスト用の注文を作る
        return new Order("O1", customer, List.of(new OrderLine("P1", 1000, quantity)), null, LocalDate.of(2026, 5, 10));
    }

    /**
     * テスト対象の注文処理を、指定の部品で組み立てます。
     *
     * @param listeners 通知先の一覧
     * @return 注文処理
     */
    private ExerciseOrderProcessor processor(List<OrderPlacedListener> listeners) {
        // 商品IDごとの在庫数を入れるMapを作る
        Map<String, Integer> stock = new HashMap<>();
        // 在庫に、P1を10個登録する
        stock.put("P1", 10);
        // ★重要★ 通知先の一覧を渡して、注文処理を組み立てる
        return new ExerciseOrderProcessor(stock, out, listeners);
    }

    /** 独自の通知先を、既存クラスを変更せずに追加でき、登録順に呼ばれる。 */
    @Test
    @DisplayName("独自の通知先を、既存クラスを変更せずに追加でき、登録順に呼ばれる")
    void customListenersAreCalledInOrder() {
        // ラムダ式で、独自の通知先を2つ作って渡す。1つ目は注文IDと請求額を記録し、2つ目は呼ばれたことだけ記録する
        List<String> calls = new ArrayList<>();
        // ラムダ式で「注文IDと請求額を記録する」通知先を作る
        OrderPlacedListener first = (o, r) -> calls.add("first:" + o.id() + ":" + r.total());
        // ラムダ式で「呼ばれたら second と記録する」通知先を作る
        OrderPlacedListener second = (o, r) -> calls.add("second");
        // ★重要★ 2つの通知先を渡した注文処理で、注文を処理する
        processor(List.of(first, second)).process(order(2));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of("first:O1:2000", "second"), calls);
    }

    /** 通知先が空なら、メールも監査ログも出力されない。 */
    @Test
    @DisplayName("通知先が空なら、メールも監査ログも出力されない")
    void noListenersNoNotifications() {
        // 通知先を1つも渡さなければ、本体の処理は動くが、通知は何も出ない
        processor(List.of()).process(order(2));
        // 記録した出力を、文字列にする
        String text = buffer.toString(StandardCharsets.UTF_8);
        // 出力に「[MAIL]」が含まれないことを確かめる
        assertFalse(text.contains("[MAIL]"));
        // 出力に「[AUDIT]」が含まれないことを確かめる
        assertFalse(text.contains("[AUDIT]"));
    }

    /** 注文が失敗したときは、通知先は呼ばれない。 */
    @Test
    @DisplayName("注文が失敗したときは、通知先は呼ばれない")
    void listenersNotCalledOnFailure() {
        // 在庫10個に11個の注文は失敗する。成立していない注文のことは、通知先に知らせない
        List<String> calls = new ArrayList<>();
        // ラムダ式で「呼ばれたら記録する」通知先を作る
        OrderPlacedListener listener = (o, r) -> calls.add("called");
        // 例外が投げられることを確かめる
        assertThrows(OutOfStockException.class, () -> processor(List.of(listener)).process(order(11)));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of(), calls);
    }
}
