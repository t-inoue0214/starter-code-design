package com.example.orders.ch09_mvc;

import com.example.orders.ch09_mvc.exercise.ExerciseOrderApp;
import com.example.orders.contract.OrderAppContractTest;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第9章の課題の実装が、分解前と同じ契約テストを満たすことを確かめます。
 */
class ExerciseOrderAppTest extends OrderAppContractTest {
    @Override
    protected OrderApp create(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        return new ExerciseOrderApp(stock, out, today);
    }
}
