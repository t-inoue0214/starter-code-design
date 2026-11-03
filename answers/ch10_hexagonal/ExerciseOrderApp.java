package com.example.orders.ch10_hexagonal.exercise;

import com.example.orders.ch10_hexagonal.exercise.adapter.inbound.ConsoleCommandAdapter;
import com.example.orders.ch10_hexagonal.exercise.adapter.outbound.ConsoleNotificationAdapter;
import com.example.orders.ch10_hexagonal.exercise.adapter.outbound.InMemoryOrderStoreAdapter;
import com.example.orders.ch10_hexagonal.exercise.adapter.outbound.InMemoryStockAdapter;
import com.example.orders.ch10_hexagonal.exercise.core.OrderService;
import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;

/**
 * 第10章の解答例です。中心とアダプタを組み立てる役（コンポジションルート）だけが残っています。
 */
public class ExerciseOrderApp implements OrderApp {
    private final OrderApp inbound;

    /**
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // 出口のアダプタを作り、中心に渡す
        OrderService core = new OrderService(
                new InMemoryStockAdapter(stock),
                new InMemoryOrderStoreAdapter(),
                new ConsoleNotificationAdapter(out));
        // 入口のアダプタに、中心を入口のポートとして渡す
        this.inbound = new ConsoleCommandAdapter(core, today);
    }

    @Override
    public String handle(String commandLine) {
        return inbound.handle(commandLine);
    }
}
