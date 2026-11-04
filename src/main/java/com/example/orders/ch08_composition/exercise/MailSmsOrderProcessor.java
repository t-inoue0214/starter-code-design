package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * メール通知つきの処理に、SMS通知を足した子クラスです。
 */
public class MailSmsOrderProcessor extends MailOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public MailSmsOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        super(stock, out);
    }

    /**
     * メール通知つきの処理に続けて、SMSで知らせます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        OrderResult result = super.process(order);
        if (order.customer().phone() != null && result.total() >= 10000) {
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }
        return result;
    }
}
