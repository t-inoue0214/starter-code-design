package com.example.orders.ch08_composition.before;

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
        // 親クラスのコンストラクタに、在庫と出力先を渡す
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
        // 親クラスの処理（検証から決済まで）を先に実行する
        OrderResult result = super.process(order);
        // 電話番号があり、請求額が1万円以上のときだけ
        if (order.customer().phone() != null && result.total() >= 10000) {
            // SMS通知を出力する（宛先、注文ID）
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }
        // 処理の結果を、そのまま返す
        return result;
    }
}
