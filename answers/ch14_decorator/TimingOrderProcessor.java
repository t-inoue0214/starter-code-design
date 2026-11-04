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
    // 包まれる本体を持つフィールド
    private final OrderProcessor delegate;
    // 出力先を持つフィールド
    private final PrintStream out;
    // nanoClockを持つフィールド
    private final LongSupplier nanoClock;

    /**
     * @param delegate  包む対象の注文処理
     * @param out       計測結果の出力先
     * @param nanoClock 現在時刻（ナノ秒）を返すもの
     */
    public TimingOrderProcessor(OrderProcessor delegate, PrintStream out, LongSupplier nanoClock) {
        // 引数の包まれる本体を、フィールドに保存する
        this.delegate = delegate;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
        // 引数のnanoClockを、フィールドに保存する
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
        // 開始時刻を、時計から読み取る
        long startedAt = nanoClock.getAsLong();
        // 例外が起こりうる処理を、ここから試す
        try {
            // ★重要★ 本来の処理は、包んでいる本体に任せる
            return delegate.process(order);
        // 成功しても失敗しても、最後に必ず実行する
        } finally {
            // 成功でも失敗でも、かかった時間を出力する
            long elapsedMillis = (nanoClock.getAsLong() - startedAt) / 1_000_000;
            // ログに出す注文IDを取り出す（注文がなければ null）
            String orderId = order == null ? null : order.id();
            // かかった時間を出力する
            out.println("[TIME] order=" + orderId + " elapsed=" + elapsedMillis + "ms");
        }
    }
}
