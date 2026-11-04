package com.example.orders.ch05_ecb.exercise;

import com.example.orders.ch05_ecb.exercise.boundary.ConsoleOrderBoundary;
import com.example.orders.ch05_ecb.exercise.control.OrderControl;
import com.example.orders.ch05_ecb.exercise.entity.OrderBook;
import com.example.orders.ch05_ecb.exercise.entity.Stock;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第5章の解答例です。部品を組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    // boundaryを持つフィールド
    private final OrderApp boundary;

    /**
     * Boundary / Control / Entity を組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // ★重要★ 依存される側（Entity）から順に作り、使う側に渡す
        OrderControl control = new OrderControl(new Stock(stock), new OrderBook());
        // ★重要★ 外との接点には、処理の流れ（Control）だけを渡す（Entity は渡さない）
        this.boundary = new ConsoleOrderBoundary(control, out, today);
    }

    /**
     * コマンド文字列を解釈して処理し、応答の文字列を返します。
     *
     * @param commandLine 受け取ったコマンド文字列
     * @return 応答の文字列（失敗したときは ERROR で始まる）
     */
    @Override
    public String handle(String commandLine) {
        // 外との接点（Boundary）に処理を任せ、その応答を返す
        return boundary.handle(commandLine);
    }
}
