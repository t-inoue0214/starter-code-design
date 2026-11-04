package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * メールで知らせます。
 */
public class MailFollowUp implements OrderFollowUp {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public MailFollowUp(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * メールで知らせます。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void run(Order order, OrderResult result) {
        // メールアドレスがあるときだけ
        if (order.customer().email() != null) {
            // メール通知を出力する（宛先、注文ID、請求額）
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + result.total());
        }
    }
}
