package com.example.orders.ch04_decorator.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文処理を包んで、開始・成功・失敗のログを出力するデコレータです。
 * TODO: 本体から取り除いたログ出力を、ここへ移してください。
 */
public class LoggingOrderProcessor implements OrderProcessor {
    private final OrderProcessor delegate;
    private final PrintStream out;

    /**
     * @param delegate 包む対象の注文処理
     * @param out      ログの出力先
     */
    public LoggingOrderProcessor(OrderProcessor delegate, PrintStream out) {
        this.delegate = delegate;
        this.out = out;
    }

    @Override
    public OrderResult process(Order order) {
        // TODO: ここにログ出力を書く。今は何もせず、そのまま委譲するだけ
        return delegate.process(order);
    }
}
