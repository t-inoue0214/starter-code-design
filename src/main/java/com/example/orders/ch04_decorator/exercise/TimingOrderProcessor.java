package com.example.orders.ch04_decorator.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.function.LongSupplier;

/**
 * 注文処理を包んで、実行時間を出力するデコレータです。
 * TODO: 本体から取り除いた時間の計測を、ここへ移してください。
 */
public class TimingOrderProcessor implements OrderProcessor {
    private final OrderProcessor delegate;
    private final PrintStream out;
    private final LongSupplier nanoClock;

    /**
     * @param delegate  包む対象の注文処理
     * @param out       計測結果の出力先
     * @param nanoClock 現在時刻（ナノ秒）を返すもの。テストでは偽物に差し替える
     */
    public TimingOrderProcessor(OrderProcessor delegate, PrintStream out, LongSupplier nanoClock) {
        this.delegate = delegate;
        this.out = out;
        this.nanoClock = nanoClock;
    }

    @Override
    public OrderResult process(Order order) {
        // TODO: ここに時間の計測を書く。今は何もせず、そのまま委譲するだけ
        return delegate.process(order);
    }
}
