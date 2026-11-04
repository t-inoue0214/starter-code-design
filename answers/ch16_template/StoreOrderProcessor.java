package com.example.orders.ch16_template.exercise;

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
        // 親クラスのコンストラクタに、在庫と出力先を渡す
        super(stock, out);
    }

    /**
     * 決済の上限額（円）を返します。
     *
     * @return 上限額（円）
     */
    @Override
    protected int paymentLimit() {
        // 店頭は100万円まで受け付ける
        return 1000000;
    }

    /**
     * 店頭では、メールもSMSも送りません。
     *
     * @param order  注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    protected void notifyCustomer(Order order, OrderResult result) {
        // ★重要★ 店頭では、その場で伝えるため何もしない
    }

    /**
     * 監査ログの末尾に付ける文字列を返します。
     *
     * @return 末尾に付ける文字列
     */
    @Override
    protected String auditSuffix() {
        // 店頭を表す印を返す
        return " channel=STORE";
    }
}
