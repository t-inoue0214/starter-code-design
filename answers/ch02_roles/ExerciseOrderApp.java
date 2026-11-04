package com.example.orders.ch02_roles.exercise;

import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第2章の解答例です。部品を組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    // 入口の担当を持つフィールド
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
        // 注文の保存先を作る
        OrderRepository orders = new OrderRepository();
        // ★重要★ 業務ロジックには、基盤寄りの部品（保存先と通知）を渡して組み立てる
        OrderService service = new OrderService(stocks, orders, new ConsoleNotifier(out));
        // ★重要★ 入口には、業務ロジックだけを渡す（入口は保存先を直接知らない）
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
        // 入口のクラスに処理を任せ、その応答を返す
        return controller.handle(commandLine);
    }
}
