package com.example.orders.domain;

/**
 * 注文アプリの公開窓口です（第4〜7章と第10・11章の共通）。
 * コマンド文字列を1行受け取り、応答文字列を1行返します。
 *
 * <pre>
 * PLACE 注文ID 顧客ID ランク 商品ID:単価:数量[;商品ID:単価:数量...]  → OK 注文ID total=金額
 * SHOW 注文ID                                                         → ORDER 注文ID customer=顧客ID total=金額
 * HISTORY 顧客ID                                                      → HISTORY 顧客ID 注文ID,注文ID,...
 * </pre>
 *
 * 失敗したときは、例外ではなく「ERROR 理由」を返します。
 */
public interface OrderApp {
    /**
     * コマンドを1つ処理します。
     *
     * @param commandLine コマンド文字列
     * @return 応答文字列
     */
    String handle(String commandLine);
}
