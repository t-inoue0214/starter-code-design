package com.example.orders.ch06_template;

import com.example.orders.ch06_template.before.WebOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第6章の分解前のWeb注文が、共通の契約テストを満たすことを確かめます。
 */
class WebOrderProcessorBeforeTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new WebOrderProcessor(stock, out);
    }
}
