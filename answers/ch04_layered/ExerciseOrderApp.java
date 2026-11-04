package com.example.orders.ch04_layered.exercise;

import com.example.orders.ch04_layered.exercise.persistence.OrderRepository;
import com.example.orders.ch04_layered.exercise.persistence.StockRepository;
import com.example.orders.ch04_layered.exercise.presentation.OrderCommandController;
import com.example.orders.ch04_layered.exercise.service.OrderNotifier;
import com.example.orders.ch04_layered.exercise.service.OrderService;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第4章の解答例です。部品を組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    // 入口の担当を持つフィールド
    private final OrderApp controller;

    /**
     * 3つの層を組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // ★重要★ 下の層から順に作り、上の層に渡す
        StockRepository stocks = new StockRepository(stock);
        // 注文の保存先を作る
        OrderRepository orders = new OrderRepository();
        // ★重要★ 業務層に、永続化層の部品と通知を渡して組み立てる
        OrderService service = new OrderService(stocks, orders, new OrderNotifier(out));
        // ★重要★ 表示層には、業務層だけを渡す（表示層は永続化層を直接知らない）
        this.controller = new OrderCommandController(service, today);
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
