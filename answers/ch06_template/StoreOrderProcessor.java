package com.example.orders.ch06_template.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * 店頭注文用の実装です。上限額が100万円で、通知はせず、監査ログに店頭の印を付けます。
 */
public class StoreOrderProcessor extends AbstractOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public StoreOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        super(stock, out);
    }

    @Override
    protected int paymentLimit() {
        // 店頭は100万円まで受け付ける
        return 1000000;
    }

    @Override
    protected void notifyCustomer(Order order, OrderResult result) {
        // 店頭では、その場で伝えるため何もしない
    }

    @Override
    protected String auditSuffix() {
        return " channel=STORE";
    }
}
