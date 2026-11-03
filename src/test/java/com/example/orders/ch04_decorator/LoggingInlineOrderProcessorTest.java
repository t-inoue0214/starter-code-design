package com.example.orders.ch04_decorator;

import com.example.orders.ch04_decorator.before.LoggingInlineOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第4章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class LoggingInlineOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new LoggingInlineOrderProcessor(stock, out);
    }
}
