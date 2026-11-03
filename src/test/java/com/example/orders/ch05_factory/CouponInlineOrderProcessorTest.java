package com.example.orders.ch05_factory;

import com.example.orders.ch05_factory.before.CouponInlineOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第5章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class CouponInlineOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new CouponInlineOrderProcessor(stock, out);
    }
}
