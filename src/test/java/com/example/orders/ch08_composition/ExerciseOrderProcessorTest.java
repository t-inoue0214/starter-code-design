package com.example.orders.ch08_composition;

import com.example.orders.ch08_composition.exercise.ExerciseOrderProcessor;
import com.example.orders.contract.OrderProcessorContractTest;
import com.example.orders.domain.OrderProcessor;
import java.io.PrintStream;
import java.util.Map;

/**
 * 第8章の課題の実装が、分解前と同じ契約テストを満たすことを確かめます。
 * このテストは、分解の前後で書き換えません。
 */
class ExerciseOrderProcessorTest extends OrderProcessorContractTest {
    /**
     * 共通の契約テストが使う、テスト対象の注文処理を生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out 出力先
     * @return テスト対象
     */
    @Override
    protected OrderProcessor create(Map<String, Integer> stock, PrintStream out) {
        return ExerciseOrderProcessor.standard(stock, out);
    }
}
