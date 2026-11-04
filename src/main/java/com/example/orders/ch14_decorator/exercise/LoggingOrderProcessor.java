package com.example.orders.ch14_decorator.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文処理を包んで、開始・成功・失敗のログを出力するデコレータです。
 * TODO: 本体から取り除いたログ出力を、ここへ移してください。
 */
public class LoggingOrderProcessor implements OrderProcessor {
    // 包まれる本体を持つフィールド
    private final OrderProcessor delegate;
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param delegate 包む対象の注文処理
     * @param out      ログの出力先
     */
    public LoggingOrderProcessor(OrderProcessor delegate, PrintStream out) {
        // 引数の包まれる本体を、フィールドに保存する
        this.delegate = delegate;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 本体を呼ぶ前後でログを出力します。結果は変えず、失敗は記録して投げ直します。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // TODO: ここにログ出力を書く。今は何もせず、そのまま委譲するだけ
        return delegate.process(order);
    }
}
