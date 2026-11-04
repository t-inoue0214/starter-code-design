package com.example.orders.ch16_template.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * Web注文用の実装です。メールとSMSで通知します。
 */
public class WebOrderProcessor extends AbstractOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public WebOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 親クラスのコンストラクタに、在庫と出力先を渡す
        super(stock, out);
    }

    /**
     * メールとSMSで知らせます。
     *
     * @param order 注文
     * @param result 処理の結果（金額の内訳）
     */
    @Override
    protected void notifyCustomer(Order order, OrderResult result) {
        // メールアドレスがあればメールで通知する
        if (order.customer().email() != null) {
            // メール通知を出力する（宛先、注文ID、請求額）
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + result.total());
        }
        // 電話番号があり、1万円以上ならSMSでも通知する
        if (order.customer().phone() != null && result.total() >= 10000) {
            // SMS通知を出力する（宛先、注文ID）
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }
    }
}
