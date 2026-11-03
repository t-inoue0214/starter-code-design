package com.example.orders.ch02_strategy;

import com.example.orders.ch02_strategy.exercise.ExerciseOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第2章の課題の実装が、分解前と同じ契約テストを満たすことを確かめます。
 * このテストは、分解の前後で書き換えません。
 */
class ExerciseOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new ExerciseOrderProcessor(stock, out);
    }
}
