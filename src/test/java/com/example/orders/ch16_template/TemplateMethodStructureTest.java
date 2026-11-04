package com.example.orders.ch16_template;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.ch16_template.exercise.AbstractOrderProcessor;
import com.example.orders.ch16_template.exercise.StoreOrderProcessor;
import com.example.orders.ch16_template.exercise.WebOrderProcessor;
import com.example.orders.domain.Order;

/**
 * 手順が1カ所に集まり、子クラスには差分だけが残っていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class TemplateMethodStructureTest {
    /** 手順（process）は抽象クラスにあり、final で固定されている。 */
    @Test
    @DisplayName("手順（process）は抽象クラスにあり、final で固定されている")
    void templateMethodIsFinalInAbstractClass() throws NoSuchMethodException {
        // ★重要★ 手順を固定するため、process は final にして、子クラスが上書きできないようにする
        Method m = AbstractOrderProcessor.class.getDeclaredMethod("process", Order.class);
        // 条件が成り立つことを確かめる
        assertTrue(Modifier.isFinal(m.getModifiers()));
    }

    /** WebとStoreは抽象クラスを継承している。 */
    @Test
    @DisplayName("WebとStoreは抽象クラスを継承している")
    void subclassesExtendAbstractClass() {
        // 条件が成り立つことを確かめる
        assertTrue(AbstractOrderProcessor.class.isAssignableFrom(WebOrderProcessor.class));
        // 条件が成り立つことを確かめる
        assertTrue(AbstractOrderProcessor.class.isAssignableFrom(StoreOrderProcessor.class));
    }

    /** WebとStoreは、手順（process）を自分で持っていない（コピーが残っていない）。 */
    @Test
    @DisplayName("WebとStoreは、手順（process）を自分で持っていない（コピーが残っていない）")
    void subclassesDoNotRedefineProcess() {
        // ★重要★ 子クラスに process が残っていれば、コピーが残っている証拠になる
        assertThrows(NoSuchMethodException.class,
                () -> WebOrderProcessor.class.getDeclaredMethod("process", Order.class));
        // 例外が投げられることを確かめる
        assertThrows(NoSuchMethodException.class,
                () -> StoreOrderProcessor.class.getDeclaredMethod("process", Order.class));
        // 条件が成り立たないことを確かめる
        assertFalse(Modifier.isAbstract(WebOrderProcessor.class.getModifiers()));
    }
}
