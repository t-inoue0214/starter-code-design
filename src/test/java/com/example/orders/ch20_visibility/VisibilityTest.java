package com.example.orders.ch20_visibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch20_visibility.app.CheckoutService;
import com.example.orders.ch20_visibility.before.OpenOrder;
import com.example.orders.ch20_visibility.order.Order;
import com.example.orders.ch20_visibility.order.OrderLine;
import com.example.orders.ch20_visibility.order.OrderStatus;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 見せる範囲（可視性）の違いを、動かして確かめるテストです。
 * このテストは、注文のパッケージの「外」に置いています。そのため、外から見える操作だけを使えます。
 * このテストは、最初から全て green です。
 */
class VisibilityTest {

    /** すべて public だと、外から決まりを破れる。 */
    @Test
    @DisplayName("すべて public の注文は、行が空のまま外から確定済みにできてしまう")
    void openOrderCanBePlacedWhileEmpty() {
        // 空の下書きの注文を作る
        OpenOrder order = OpenOrder.draft("C001");
        // 外から、状態を直接書き換える
        order.setStatus("PLACED");
        // 行が空なのに、確定済みになってしまう
        assertEquals("PLACED", order.status());
        // 行は1つもない
        assertTrue(order.lines().isEmpty());
    }

    /** 中の手順が public だと、使う側が呼び忘れる。 */
    @Test
    @DisplayName("すべて public の注文は、上限を迂回でき、recalculate を呼び忘れると合計が古い")
    void openOrderLeaksItsInternals() {
        // 空の下書きの注文を作る
        OpenOrder order = OpenOrder.draft("C001");
        // 上限の範囲内で、商品を足す
        order.addItem("P001", 1000, 1);
        // 受け取った行から、上限を超える数量に書き換える
        order.lines().get(0).setQuantity(1000);
        // recalculate を呼び忘れたので、合計は0のまま
        assertEquals(0, order.total());
        // 呼べば計算されるが、上限を超えた数量で計算される
        order.recalculate();
        // 上限の99個を超えた1000個分の合計になってしまう
        assertEquals(1_000_000, order.total());
    }

    /** 公開された操作だけで、正しく使える。 */
    @Test
    @DisplayName("Order は、公開された操作だけで、同じ商品をまとめて合計を出せる")
    void orderWorksThroughItsPublicApi() {
        // 空の下書きの注文を作る
        Order order = Order.create("C001");
        // 商品を2個足す
        order.addItem("P001", 1000, 2);
        // 同じ商品を、さらに3個足す
        order.addItem("P001", 1000, 3);
        // 同じ商品は、1行にまとまる
        assertEquals(1, order.lines().size());
        // 合計は、読んだ時点で計算される
        assertEquals(5000, order.total());
        // 確定する
        order.place();
        // 確定済みになる
        assertEquals(OrderStatus.PLACED, order.status());
    }

    /** 決まりを破る操作は、公開された入り口で拒否される。 */
    @Test
    @DisplayName("Order は、上限超え、空の確定、確定後の追加、返した一覧の変更を拒否する")
    void orderRejectsBrokenRules() {
        // 空の下書きの注文を作る
        Order order = Order.create("C001");
        // 上限を超える数量は、拒否される
        assertThrows(IllegalArgumentException.class, () -> order.addItem("P001", 1000, 100));
        // 行のない注文の確定は、拒否される
        assertThrows(IllegalStateException.class, order::place);
        // 商品を1個足す
        order.addItem("P001", 1000, 1);
        // 返された一覧は、変更できない
        assertThrows(UnsupportedOperationException.class, () -> order.lines().clear());
        // 確定する
        order.place();
        // 確定後の追加は、拒否される
        assertThrows(IllegalStateException.class, () -> order.addItem("P002", 500, 1));
    }

    /** public なメソッドは、決めた操作だけ。 */
    @Test
    @DisplayName("Order の public なメソッドは、決めた7つだけ")
    void orderExposesOnlyItsOperations() {
        // public なメソッドの名前を、名前順に集める
        Set<String> names = new TreeSet<>();
        // 宣言されたメソッドを1つずつ見る
        for (Method m : Order.class.getDeclaredMethods()) {
            // public なメソッドのとき
            if (Modifier.isPublic(m.getModifiers())) {
                // 名前を集める
                names.add(m.getName());
            }
        }
        // 作る、足す、確定する、読む、の操作だけが公開されている
        assertEquals(Set.of("create", "addItem", "place", "total", "customerId", "status", "lines"), names);
        // コンストラクタは、どれも public ではない
        for (Constructor<?> c : Order.class.getDeclaredConstructors()) {
            // public でないことを確かめる
            assertFalse(Modifier.isPublic(c.getModifiers()));
        }
    }

    /** 行は読めるが、外では作れない。中の決まりは見えない。 */
    @Test
    @DisplayName("OrderLine は外で作れず、PriceRules はパッケージの外から見えない")
    void linesAndRulesAreHidden() throws ClassNotFoundException {
        // 行のコンストラクタを、1つずつ見る
        for (Constructor<?> c : OrderLine.class.getDeclaredConstructors()) {
            // public でないことを確かめる
            assertFalse(Modifier.isPublic(c.getModifiers()));
        }
        // 金額の決まりのクラスを、名前で探す
        Class<?> rules = Class.forName("com.example.orders.ch20_visibility.order.PriceRules");
        // クラスが public でないことを確かめる
        assertFalse(Modifier.isPublic(rules.getModifiers()));
    }

    /** 別のパッケージのサービスからも、公開された操作で使える。 */
    @Test
    @DisplayName("別のパッケージの CheckoutService は、公開された操作だけで注文を確定できる")
    void checkoutUsesOnlyPublicApi() {
        // サービスを作る
        CheckoutService service = new CheckoutService();
        // 注文を確定する
        Order order = service.checkout("C001", "P001", 1200, 2);
        // 確定済みになっている
        assertEquals(OrderStatus.PLACED, order.status());
        // 合計が正しい
        assertEquals(2400, order.total());
    }
}
