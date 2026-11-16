package com.example.orders.ch19_immutability;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * イミュータブルな注文の下書きです。技法: 変更メソッドが、新しいインスタンスを返すこと。
 * 状態の移り変わり（下書き → 確定）も、別の型の新しいインスタンスで表します。
 */
public final class OrderDraft {
    // 顧客IDを持つフィールド
    private final String customerId;
    // 行の一覧を持つフィールド（変更できない List）
    private final List<CartLine> lines;

    /**
     * 下書きを作ります。外からは empty と add で作ります。
     *
     * @param customerId 顧客ID
     * @param lines      行の一覧
     */
    private OrderDraft(String customerId, List<CartLine> lines) {
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の一覧を、変更できないコピーにして保存する
        this.lines = List.copyOf(lines);
    }

    /**
     * 行のない、空の下書きを作ります。
     *
     * @param customerId 顧客ID
     * @return 空の下書き
     */
    public static OrderDraft empty(String customerId) {
        // 空の一覧で、下書きを作って返す
        return new OrderDraft(customerId, List.of());
    }

    /**
     * 行を足した、新しい下書きを返します。この下書き自身は変わりません。
     *
     * @param line 足す行
     * @return 行を足した、新しい下書き
     */
    public OrderDraft add(CartLine line) {
        // 今の行の一覧を、作業用にコピーする
        List<CartLine> next = new ArrayList<>(lines);
        // 作業用の一覧に、行を足す
        next.add(line);
        // ★重要★ 自分は変えず、足した一覧を持つ新しい下書きを返す
        return new OrderDraft(customerId, next);
    }

    /**
     * 下書きを確定して、確定済みの注文を作ります。下書きは、そのまま残ります。
     *
     * @param orderId     注文ID
     * @param confirmedOn 確定日
     * @return 確定済みの注文
     */
    public ConfirmedOrder confirm(String orderId, LocalDate confirmedOn) {
        // 行がないとき
        if (lines.isEmpty()) {
            // 確定できないので拒否する
            throw new IllegalStateException("行のない下書きは確定できません");
        }
        // ★重要★ 状態を書き換えるのではなく、「確定済み」という別の型を新しく作る
        return new ConfirmedOrder(orderId, customerId, lines, confirmedOn);
    }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 行の一覧（変更できない） */
    public List<CartLine> lines() { return lines; }
}
