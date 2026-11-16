package com.example.orders.ch19_immutability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch19_immutability.before.LeakyCart;
import com.example.orders.ch19_immutability.before.LeakyMonthlyTargets;
import com.example.orders.ch19_immutability.before.MutableSku;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * イミュータブル（変わらないオブジェクト）にする技法を、動かして確かめるテストです。
 * before パッケージの「漏れている実装」と、技法を使った実装を、同じ操作で比べます。
 * このテストは、最初から全て green です。
 */
class ImmutabilityTest {

    /**
     * 1000円の商品を1個入れた行を作る補助メソッドです。
     *
     * @param code 商品コード
     * @return 作った行
     */
    private static CartLine line(String code) {
        // 単価1000円、数量1の行を作って返す
        return new CartLine(new Sku(code), Money.of(1000), Quantity.of(1));
    }

    /** 可変なキーは、Set の中で迷子になる。 */
    @Test
    @DisplayName("可変なオブジェクトを Set に入れて変更すると、Set から見つからなくなる")
    void mutableKeyGetsLostInSet() {
        // 可変な商品コードを作る
        MutableSku mutable = new MutableSku("A001");
        // Set を用意する
        Set<MutableSku> set = new HashSet<>();
        // Set に入れる
        set.add(mutable);
        // 入れたあとで、コードを変える
        mutable.changeCode("B002");
        // 同じオブジェクトなのに、Set からは見つからない（ハッシュ値が変わったため）
        assertFalse(set.contains(mutable));
        // イミュータブルな商品コードなら、変えられないので、いつでも見つかる
        Set<Sku> safe = new HashSet<>(List.of(new Sku("A001")));
        // 正規化された同じ値なら、別のインスタンスでも見つかる
        assertTrue(safe.contains(new Sku(" a001 ")));
    }

    /** List をそのまま持つと、外から変えられる。 */
    @Test
    @DisplayName("受け取った List をそのまま持つと、呼び出し側の変更がカートに漏れる")
    void leakyCartSharesTheList() {
        // 呼び出し側の List を用意する
        List<CartLine> source = new ArrayList<>(List.of(line("A001")));
        // 漏れているカートを作る
        LeakyCart leaky = new LeakyCart(source);
        // 防御的コピーをするカートを作る
        Cart cart = new Cart(source);
        // 作ったあとで、呼び出し側の List に行を足す
        source.add(line("B002"));
        // 漏れているカートは、2行に増えてしまう（入力の漏れ）
        assertEquals(2, leaky.lines().size());
        // 防御的コピーをしたカートは、1行のまま
        assertEquals(1, cart.lines().size());
        // 漏れているカートは、返された List から中身を変えられる（出力の漏れ）
        leaky.lines().clear();
        // 中身が空になってしまう
        assertTrue(leaky.lines().isEmpty());
        // 防御的コピーをしたカートは、変更しようとすると例外になる
        assertThrows(UnsupportedOperationException.class, () -> cart.lines().clear());
    }

    /** unmodifiableList は「読み取り専用の窓」で、コピーではない。 */
    @Test
    @DisplayName("Collections.unmodifiableList は元の List の変更が見えるが、List.copyOf は見えない")
    void unmodifiableViewIsNotACopy() {
        // 元の List を用意する
        List<String> source = new ArrayList<>(List.of("A"));
        // 読み取り専用の窓を作る
        List<String> view = Collections.unmodifiableList(source);
        // 変更できないコピーを作る
        List<String> copy = List.copyOf(source);
        // 元の List に要素を足す
        source.add("B");
        // 窓からは、足した要素が見えてしまう
        assertEquals(2, view.size());
        // コピーは、作った時点のまま
        assertEquals(1, copy.size());
    }

    /** final な配列は、中身を守らない。 */
    @Test
    @DisplayName("final な配列フィールドでも、コピーしないと中身を外から変えられる")
    void finalArrayDoesNotProtectContents() {
        // 12か月分の目標を用意する
        int[] source = new int[12];
        // 1月の目標を 100 にする
        source[0] = 100;
        // 漏れている実装を作る
        LeakyMonthlyTargets leaky = new LeakyMonthlyTargets(source);
        // 防御的コピーをする実装を作る
        MonthlyTargets safe = new MonthlyTargets(source);
        // 作ったあとで、呼び出し側の配列を書き換える
        source[0] = 999;
        // 漏れている実装は、書き換えが見えてしまう
        assertEquals(999, leaky.target(1));
        // 防御的コピーをした実装は、作った時点のまま
        assertEquals(100, safe.target(1));
        // 返された配列を書き換えても
        safe.toArray()[0] = 555;
        // 中身は変わらない
        assertEquals(100, safe.target(1));
    }

    /** 計算は、新しいインスタンスを返す。 */
    @Test
    @DisplayName("金額と数量は、作る時点で検証され、計算しても元の値は変わらない")
    void valueObjectsReturnNewInstances() {
        // 100円を作る
        Money hundred = Money.of(100);
        // 50円を足す
        Money sum = hundred.plus(Money.of(50));
        // 結果は150円
        assertEquals(150, sum.yen());
        // 元の100円は、変わらない
        assertEquals(100, hundred.yen());
        // 負の金額は作れない
        assertThrows(IllegalArgumentException.class, () -> Money.of(-1));
        // 0円は、共有の定数が返る
        assertSame(Money.ZERO, Money.of(0));
        // 数量 98 に 2 を足すと、範囲外なので拒否される
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(98).plus(2));
    }

    /** with メソッドは、一部だけ変えた新しいインスタンスを返す。 */
    @Test
    @DisplayName("with メソッドは、数量だけを変えた新しい行を返し、元の行は変わらない")
    void withMethodReturnsModifiedCopy() {
        // 数量1の行を作る
        CartLine original = line("A001");
        // 数量を3に変えた行を作る
        CartLine changed = original.withQuantity(Quantity.of(3));
        // 新しい行は、別のインスタンス
        assertNotSame(original, changed);
        // 新しい行の数量は3
        assertEquals(3, changed.quantity().value());
        // 元の行の数量は1のまま
        assertEquals(1, original.quantity().value());
        // 商品コードは、引き継がれる
        assertEquals("A001", changed.sku().code());
    }

    /** 変更メソッドと状態の移り変わりも、新しいインスタンスで表す。 */
    @Test
    @DisplayName("下書きに行を足しても元の下書きは変わらず、確定すると別の型の注文になる")
    void stateTransitionCreatesNewObjects() {
        // 空の下書きを作る
        OrderDraft empty = OrderDraft.empty("C1");
        // 行を1つ足した下書きを作る
        OrderDraft one = empty.add(line("A001"));
        // 元の下書きは、空のまま
        assertTrue(empty.lines().isEmpty());
        // 新しい下書きは、1行
        assertEquals(1, one.lines().size());
        // 確定すると、確定済みの注文ができる
        ConfirmedOrder confirmed = one.confirm("O1", LocalDate.of(2026, 11, 1));
        // 合計は1000円
        assertEquals(Money.of(1000), confirmed.total());
        // 確定後も、下書きは残っていて、さらに行を足せる
        assertEquals(2, one.add(line("B002")).lines().size());
        // 確定済みの注文は、1行のまま
        assertEquals(1, confirmed.lines().size());
        // 空の下書きは、確定できない
        assertThrows(IllegalStateException.class, () -> empty.confirm("O2", LocalDate.of(2026, 11, 1)));
    }

    /** 技法を使ったクラスは、すべて final で、フィールドもすべて private final。 */
    @Test
    @DisplayName("イミュータブルなクラスは、クラスが final で、全フィールドが private final")
    void immutableClassesAreFinalWithPrivateFinalFields() {
        // 確かめるクラスの一覧
        List<Class<?>> classes = List.of(Money.class, Quantity.class, Sku.class, CartLine.class,
                Cart.class, MonthlyTargets.class, OrderDraft.class, ConfirmedOrder.class);
        // クラスを1つずつ調べる
        for (Class<?> type : classes) {
            // クラスが final であることを確かめる（子クラスで可変にされないため）
            assertTrue(Modifier.isFinal(type.getModifiers()), type.getSimpleName() + " は final ではありません");
            // フィールドを1つずつ調べる
            for (Field field : type.getDeclaredFields()) {
                // フィールドが final であることを確かめる
                assertTrue(Modifier.isFinal(field.getModifiers()), type.getSimpleName() + "." + field.getName());
                // 共有の定数（public static）以外は、private であることを確かめる
                assertTrue(Modifier.isPrivate(field.getModifiers()) || Modifier.isStatic(field.getModifiers()),
                        type.getSimpleName() + "." + field.getName());
            }
        }
    }
}
