package com.example.orders.ch08_layered.exercise;

import com.example.orders.ch08_layered.exercise.persistence.OrderRepository;
import com.example.orders.ch08_layered.exercise.persistence.StockRepository;
import com.example.orders.ch08_layered.exercise.presentation.OrderCommandController;
import com.example.orders.ch08_layered.exercise.service.OrderNotifier;
import com.example.orders.ch08_layered.exercise.service.OrderService;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第8章の解答例です。部品を組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    private final OrderApp controller;

    /**
     * 3つの層を組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // 下の層から順に作り、上の層に渡す
        StockRepository stocks = new StockRepository(stock);
        OrderRepository orders = new OrderRepository();
        OrderService service = new OrderService(stocks, orders, new OrderNotifier(out));
        this.controller = new OrderCommandController(service, today);
    }

    @Override
    public String handle(String commandLine) {
        return controller.handle(commandLine);
    }
}
