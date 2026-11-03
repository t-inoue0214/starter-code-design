package com.example.orders.ch06_template;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch06_template.exercise.AbstractOrderProcessor;
import com.example.orders.ch06_template.exercise.StoreOrderProcessor;
import com.example.orders.ch06_template.exercise.WebOrderProcessor;
import com.example.orders.domain.Order;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 手順が1カ所に集まり、子クラスには差分だけが残っていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class TemplateMethodStructureTest {
    @Test
    @DisplayName("手順（process）は抽象クラスにあり、final で固定されている")
    void templateMethodIsFinalInAbstractClass() throws NoSuchMethodException {
        Method m = AbstractOrderProcessor.class.getDeclaredMethod("process", Order.class);
        assertTrue(Modifier.isFinal(m.getModifiers()));
    }

    @Test
    @DisplayName("WebとStoreは抽象クラスを継承している")
    void subclassesExtendAbstractClass() {
        assertTrue(AbstractOrderProcessor.class.isAssignableFrom(WebOrderProcessor.class));
        assertTrue(AbstractOrderProcessor.class.isAssignableFrom(StoreOrderProcessor.class));
    }

    @Test
    @DisplayName("WebとStoreは、手順（process）を自分で持っていない（コピーが残っていない）")
    void subclassesDoNotRedefineProcess() {
        assertThrows(NoSuchMethodException.class, () -> WebOrderProcessor.class.getDeclaredMethod("process", Order.class));
        assertThrows(NoSuchMethodException.class, () -> StoreOrderProcessor.class.getDeclaredMethod("process", Order.class));
        assertFalse(Modifier.isAbstract(WebOrderProcessor.class.getModifiers()));
    }
}
