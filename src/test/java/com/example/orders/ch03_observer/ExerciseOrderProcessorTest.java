package com.example.orders.ch03_observer;

import com.example.orders.ch03_observer.exercise.ExerciseOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第3章の課題の実装が、分解前と同じ契約テストを満たすことを確かめます。
 */
class ExerciseOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new ExerciseOrderProcessor(stock, out);
    }
}
