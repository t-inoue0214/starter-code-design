package com.example.orders.ch07_event_driven;

import com.example.orders.ch07_event_driven.before.ConsoleOrderApp;
import com.example.orders.contract.OrderAppContractTest;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第7章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class ConsoleOrderAppTest extends OrderAppContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文アプリを生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @param today 今日の日付
     * @return テスト対象
     */
    @Override
    protected OrderApp create(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        return new ConsoleOrderApp(stock, out, today);
    }
}
