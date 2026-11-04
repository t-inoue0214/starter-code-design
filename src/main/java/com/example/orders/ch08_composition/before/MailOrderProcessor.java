package com.example.orders.ch08_composition.before;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * 土台の処理に、メール通知を足した子クラスです。
 */
public class MailOrderProcessor extends BaseOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public MailOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 親クラスのコンストラクタに、在庫と出力先を渡す
        super(stock, out);
    }

    /**
     * 親の処理（検証から決済まで）に続けて、メールで知らせます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // 親クラスの処理（検証から決済まで）を先に実行する
        OrderResult result = super.process(order);
        // メールアドレスがあるときだけ
        if (order.customer().email() != null) {
            // メール通知を出力する（宛先、注文ID、請求額）
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + result.total());
        }
        // 処理の結果を、そのまま返す
        return result;
    }
}
