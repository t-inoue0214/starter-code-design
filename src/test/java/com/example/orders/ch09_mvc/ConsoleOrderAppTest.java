package com.example.orders.ch09_mvc;

import com.example.orders.ch09_mvc.before.ConsoleOrderApp;
import com.example.orders.contract.OrderAppContractTest;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第9章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class ConsoleOrderAppTest extends OrderAppContractTest {
    @Override
    protected OrderApp create(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        return new ConsoleOrderApp(stock, out, today);
    }
}
