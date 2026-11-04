package com.example.orders.ch14_decorator.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.function.LongSupplier;

/**
 * 注文処理を包んで、実行時間を出力するデコレータです。
 */
public class TimingOrderProcessor implements OrderProcessor {
    private final OrderProcessor delegate;
    private final PrintStream out;
    private final LongSupplier nanoClock;

    /**
     * @param delegate  包む対象の注文処理
     * @param out       計測結果の出力先
     * @param nanoClock 現在時刻（ナノ秒）を返すもの
     */
    public TimingOrderProcessor(OrderProcessor delegate, PrintStream out, LongSupplier nanoClock) {
        this.delegate = delegate;
        this.out = out;
        this.nanoClock = nanoClock;
    }

    /**
     * 本体の実行時間を計測して出力します。失敗したときも出力します。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        long startedAt = nanoClock.getAsLong();
        try {
            return delegate.process(order);
        } finally {
            // 成功でも失敗でも、かかった時間を出力する
            long elapsedMillis = (nanoClock.getAsLong() - startedAt) / 1_000_000;
            String orderId = order == null ? null : order.id();
            out.println("[TIME] order=" + orderId + " elapsed=" + elapsedMillis + "ms");
        }
    }
}
