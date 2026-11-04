package com.example.orders.ch06_mvc.exercise.view;

import com.example.orders.ch06_mvc.exercise.model.OrderRecord;
import java.io.PrintStream;
import java.util.List;

/**
 * 画面（コンソール）への出力と、応答の文面を担当します（View）。業務ルールは持ちません。
 */
public class OrderView {
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param out 出力先
     */
    public OrderView(PrintStream out) {
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 注文成立の通知を出力します。
     *
     * @param record 成立した注文
     */
    public void notifyPlaced(OrderRecord record) {
        // 成立した注文を、通知として出力する（出力は View の責務）
        out.println("[NOTIFY] customer=" + record.customerId() + " order=" + record.orderId() + " total=" + record.total());
    }

    /** @param record 成立した注文
     *  @return 注文成功の応答 */
    public String ok(OrderRecord record) {
        // 成功の応答の文字列を作る
        return "OK " + record.orderId() + " total=" + record.total();
    }

    /** @param record 注文
     *  @return 注文の参照結果の応答 */
    public String order(OrderRecord record) {
        // 注文の内容の応答の文字列を作る
        return "ORDER " + record.orderId() + " customer=" + record.customerId() + " total=" + record.total();
    }

    /** @param customerId 顧客ID
     *  @param ids        注文IDの一覧
     *  @return 履歴の応答 */
    public String history(String customerId, List<String> ids) {
        // 履歴の応答の文字列を作る（空なら (none)）
        return "HISTORY " + customerId + " " + (ids.isEmpty() ? "(none)" : String.join(",", ids));
    }

    /** @param message 理由
     *  @return エラーの応答 */
    public String error(String message) {
        // エラーの応答の文字列を作る
        return "ERROR " + message;
    }
}
