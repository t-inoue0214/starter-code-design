package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * メールとSMSの通知つきの処理に、監査ログを足した子クラスです。
 */
public class AuditedMailSmsOrderProcessor extends MailSmsOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public AuditedMailSmsOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        super(stock, out);
    }

    /**
     * メールとSMSの通知つきの処理に続けて、監査ログを残します。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        OrderResult result = super.process(order);
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + result.total());
        return result;
    }
}
