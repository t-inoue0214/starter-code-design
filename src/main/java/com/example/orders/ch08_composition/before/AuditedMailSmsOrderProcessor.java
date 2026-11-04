package com.example.orders.ch08_composition.before;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;
import java.io.PrintStream;
import java.util.Map;

/**
 * メールとSMSの通知つきの処理に、監査ログを足した子クラスです。この教材の「完成形」で、契約テストはこのクラスに当てます。
 */
public class AuditedMailSmsOrderProcessor extends MailSmsOrderProcessor {
    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public AuditedMailSmsOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 親クラスのコンストラクタに、在庫と出力先を渡す
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
        // 親クラスの処理（検証から決済まで）を先に実行する
        OrderResult result = super.process(order);
        // 監査ログを出力する（注文ID、顧客ID、請求額）
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + result.total());
        // 親の処理の結果を、そのまま返す
        return result;
    }
}
