package com.example.orders.ch13_observer.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 注文成立をメールで通知します（メールアドレスがある場合だけ）。
 */
public class MailNotifier implements OrderPlacedListener {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public MailNotifier(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 注文の成立を受け取って処理します。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void onOrderPlaced(Order order, OrderResult result) {
        // メールアドレスがなければ何もしない
        if (order.customer().email() == null) {
            // ここで処理を終える
            return;
        }
        // メール通知を出力する（宛先、注文ID、請求額）
        out.println("[MAIL] to=" + order.customer().email()
                + " order=" + order.id() + " total=" + result.total());
    }
}
