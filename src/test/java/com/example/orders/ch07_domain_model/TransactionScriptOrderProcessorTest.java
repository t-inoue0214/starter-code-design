package com.example.orders.ch07_domain_model;

import com.example.orders.ch07_domain_model.before.TransactionScriptOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第7章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class TransactionScriptOrderProcessorTest extends OrderProcessorContractTest {
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new TransactionScriptOrderProcessor(stock, out);
    }
}
