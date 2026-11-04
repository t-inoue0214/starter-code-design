package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;

/**
 * 監査ログを残します。
 */
public class AuditFollowUp implements OrderFollowUp {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public AuditFollowUp(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 監査ログを残します。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    public void run(Order order, OrderResult result) {
        // 監査ログを出力する（注文ID、顧客ID、請求額）
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + result.total());
    }
}
