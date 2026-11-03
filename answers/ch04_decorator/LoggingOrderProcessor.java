package com.example.orders.ch04_decorator.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文処理を包んで、開始・成功・失敗のログを出力するデコレータです。
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
        // 注文IDは null の可能性があるため、安全に取り出す
        String orderId = order == null ? null : order.id();
        out.println("[LOG] start order=" + orderId);
        try {
            // 本来の処理に委譲する
            OrderResult result = delegate.process(order);
            out.println("[LOG] end order=" + orderId + " total=" + result.total());
            return result;
        } catch (RuntimeException e) {
            // 失敗を記録し、例外はそのまま呼び出し側へ返す
            out.println("[LOG] failed order=" + orderId + " reason=" + e.getMessage());
            throw e;
        }
    }
}
