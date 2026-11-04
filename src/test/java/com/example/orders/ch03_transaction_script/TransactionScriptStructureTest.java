package com.example.orders.ch03_transaction_script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch03_transaction_script.exercise.ExerciseOrderProcessor;
import com.example.orders.contract.SourceRules;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * トランザクションスクリプトとして、手続きが読みやすく整っているかを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <ul>
 *   <li>外から呼べるのは、ユースケースを表す {@code process} だけ</li>
 *   <li>{@code process} は、手順の名前を並べた短いメソッド</li>
 *   <li>手順の中身は、名前の付いた private メソッドに分かれている</li>
 * </ul>
 */
class TransactionScriptStructureTest {
    private static final String FILE = "ch03_transaction_script/exercise/ExerciseOrderProcessor.java";

    /** 外から呼べるメソッドは、ユースケースの process だけである。 */
    @Test
    @DisplayName("外から呼べるメソッドは、ユースケースの process だけである")
    void onlyUseCaseIsPublic() {
        List<String> names = Arrays.stream(ExerciseOrderProcessor.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                .map(Method::getName)
                .collect(Collectors.toList());
        assertEquals(List.of("process"), names);
    }

    /** 手順ごとに名前の付いた private メソッドが5つ以上ある。 */
    @Test
    @DisplayName("手順ごとに名前の付いた private メソッドが5つ以上ある")
    void stepsAreNamedMethods() {
        long steps = Arrays.stream(ExerciseOrderProcessor.class.getDeclaredMethods())
                .filter(m -> Modifier.isPrivate(m.getModifiers()) && !m.isSynthetic())
                .count();
        assertTrue(steps >= 5, "private メソッドが " + steps + " 個です。手順ごとに切り出してください");
    }

    /** process は20行以内で、手順の流れだけが読める。 */
    @Test
    @DisplayName("process は20行以内で、手順の流れだけが読める")
    void processReadsAsOutline() {
        int lines = SourceRules.methodLines(FILE, "public OrderResult process(");
        assertTrue(lines <= 20, "process が " + lines + " 行あります。手順の中身は private メソッドへ移してください");
    }
}
