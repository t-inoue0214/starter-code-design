package com.example.orders.ch04_layered;

import com.example.orders.ch04_layered.exercise.ExerciseOrderApp;
import com.example.orders.contract.OrderAppContractTest;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第4章の課題の実装が、分解前と同じ契約テストを満たすことを確かめます。
 */
class ExerciseOrderAppTest extends OrderAppContractTest {
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
        // 課題の実装を、テスト対象として返す
        return new ExerciseOrderApp(stock, out, today);
    }
}
