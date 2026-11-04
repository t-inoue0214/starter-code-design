package com.example.orders.ch14_decorator.exercise;

import java.io.PrintStream;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;

/**
 * 注文処理を包んで、開始・成功・失敗のログを出力するデコレータです。
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
        // 注文IDは null の可能性があるため、安全に取り出す
        String orderId = order == null ? null : order.id();
        // 処理の開始をログに出力する
        out.println("[LOG] start order=" + orderId);
        // 例外が起こりうる処理を、ここから試す
        try {
            // ★重要★ 本来の処理に委譲する
            OrderResult result = delegate.process(order);
            // 処理の成功をログに出力する
            out.println("[LOG] end order=" + orderId + " total=" + result.total());
            // 処理の結果を、そのまま返す
            return result;
        // 本体の処理が失敗したとき
        } catch (RuntimeException e) {
            // 失敗を記録し、例外はそのまま呼び出し側へ返す
            out.println("[LOG] failed order=" + orderId + " reason=" + e.getMessage());
            // 例外を、呼び出し側へ投げ直す
            throw e;
        }
    }
}
