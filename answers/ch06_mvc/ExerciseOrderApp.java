package com.example.orders.ch06_mvc.exercise;

import com.example.orders.ch06_mvc.exercise.controller.OrderController;
import com.example.orders.ch06_mvc.exercise.model.OrderModel;
import com.example.orders.ch06_mvc.exercise.view.OrderView;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第6章の解答例です。Model・View・Controller を組み立てる役だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    private final OrderApp controller;

    /**
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        this.controller = new OrderController(new OrderModel(stock), new OrderView(out), today);
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
