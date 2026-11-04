package com.example.orders.ch08_composition;

import com.example.orders.ch08_composition.before.AuditedMailSmsOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第8章の分解前（継承版）の実装が、共通の契約テストを満たすことを確かめます。
 */
class InheritanceOrderProcessorTest extends OrderProcessorContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @return テスト対象
     */
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new AuditedMailSmsOrderProcessor(stock, out);
    }
}
