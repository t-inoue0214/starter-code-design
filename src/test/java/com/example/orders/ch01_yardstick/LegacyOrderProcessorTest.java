package com.example.orders.ch01_yardstick;

import com.example.orders.ch01_yardstick.before.LegacyOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第1章の神クラスが、共通の契約テストを満たすことを確かめます。
 */
class LegacyOrderProcessorTest extends OrderProcessorContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @return テスト対象
     */
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new LegacyOrderProcessor(stock, out);
    }
}
