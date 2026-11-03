package com.example.orders.ch04_decorator;

import com.example.orders.ch04_decorator.exercise.ExerciseOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第4章の課題の実装（本体のみ）が、共通の契約テストを満たすことを確かめます。
 */
class ExerciseOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new ExerciseOrderProcessor(stock, out);
    }
}
