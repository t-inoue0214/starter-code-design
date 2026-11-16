package com.example.orders.ch19_immutability;

import java.time.LocalDate;
import java.util.List;

/**
 * イミュータブルな確定済みの注文です。下書き（OrderDraft）の confirm からだけ作られます。
 * 「確定済み」を別の型にしたので、確定済みの注文に行を足すメソッドは、そもそも存在しません。
 */
public final class ConfirmedOrder {
    // 注文IDを持つフィールド
    private final String orderId;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 行の一覧を持つフィールド（変更できない List）
    private final List<CartLine> lines;
    // 確定日を持つフィールド（LocalDate はイミュータブルなので、コピー不要）
    private final LocalDate confirmedOn;

    /**
     * 確定済みの注文を作ります。作れるのは、同じパッケージの OrderDraft だけです。
     *
     * @param orderId     注文ID
     * @param customerId  顧客ID
     * @param lines       行の一覧
     * @param confirmedOn 確定日
     */
    ConfirmedOrder(String orderId, String customerId, List<CartLine> lines, LocalDate confirmedOn) {
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の一覧を、変更できないコピーにして保存する
        this.lines = List.copyOf(lines);
        // ★重要★ LocalDate はイミュータブルなので、そのまま持ってよい（java.util.Date なら、コピーが必要）
        this.confirmedOn = confirmedOn;
    }

    /** @return 合計金額 */
    public Money total() {
        // 行の一覧から、カートと同じ方法で合計を求めて返す
        return new Cart(lines).total();
    }

    /** @return 注文ID */
    public String orderId() { return orderId; }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 行の一覧（変更できない） */
    public List<CartLine> lines() { return lines; }

    /** @return 確定日 */
    public LocalDate confirmedOn() { return confirmedOn; }
}
