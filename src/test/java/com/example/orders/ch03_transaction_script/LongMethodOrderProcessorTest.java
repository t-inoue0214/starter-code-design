package com.example.orders.ch03_transaction_script;

import com.example.orders.ch03_transaction_script.before.LongMethodOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第3章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class LongMethodOrderProcessorTest extends OrderProcessorContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @return テスト対象
     */
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        // LongMethodOrderProcessor を、テスト対象として返す
        return new LongMethodOrderProcessor(stock, out);
    }
}
