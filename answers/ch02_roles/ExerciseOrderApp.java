package com.example.orders.ch02_roles.exercise;

import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第2章の解答例です。部品を組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    private final OrderApp controller;

    /**
     * 4つの役割のクラスを組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // ★重要★ 依存される側から順に作り、使う側に渡す
        StockRepository stocks = new StockRepository(stock);
        OrderRepository orders = new OrderRepository();
        OrderService service = new OrderService(stocks, orders, new ConsoleNotifier(out));
        this.controller = new OrderController(service, today);
    }

    /**
     * コマンド文字列を解釈して処理し、応答の文字列を返します。
     *
     * @param commandLine 受け取ったコマンド文字列
     * @return 応答の文字列（失敗したときは ERROR で始まる）
     */
    @Override
    public String handle(String commandLine) {
        return controller.handle(commandLine);
    }
}
