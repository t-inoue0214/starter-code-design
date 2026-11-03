package com.example.orders.ch03_observer;

import com.example.orders.ch03_observer.before.NotificationInlineOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第3章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class NotificationInlineOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new NotificationInlineOrderProcessor(stock, out);
    }
}
