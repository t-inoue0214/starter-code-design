package com.example.orders.ch02_strategy;

import com.example.orders.ch02_strategy.before.DiscountInlineOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第2章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class DiscountInlineOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new DiscountInlineOrderProcessor(stock, out);
    }
}
