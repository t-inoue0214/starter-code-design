package com.example.orders.ch15_factory;

import com.example.orders.ch15_factory.before.CouponInlineOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第15章の分解前の実装が、共通の契約テストを満たすことを確かめます。
 */
class CouponInlineOrderProcessorTest extends OrderProcessorContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @return テスト対象
     */
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return new CouponInlineOrderProcessor(stock, out);
    }
}
