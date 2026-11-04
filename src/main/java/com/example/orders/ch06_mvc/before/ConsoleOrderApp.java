package com.example.orders.ch06_mvc.before;

import com.example.orders.domain.OrderApp;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * コマンドの解釈、業務ルール、データの保持、画面（コンソール）出力のすべてを1クラスに詰め込んだ実装（神クラス）です。
 * 第4〜7章と第10・11章では、このクラスを、それぞれのアーキテクチャパターンで分解します。
 * 学習者は、このクラスを編集せず「読むだけ」にしてください。
 */
public class ConsoleOrderApp implements OrderApp {
    // 商品IDごとの在庫数（注文のたびに減っていく）
    private final Map<String, Integer> stock;
    // 通知の出力先
    private final PrintStream out;
    // 今日の日付
    private final LocalDate today;
    // 注文IDから顧客IDを引く表
    private final Map<String, String> orderCustomer = new HashMap<>();
    // 注文IDから請求額を引く表（注文の有無の確認にも使う）
    private final Map<String, Integer> orderTotal = new HashMap<>();
    // 顧客IDごとの注文ID一覧（古い順）
    private final Map<String, List<String>> history = new HashMap<>();

    /**
     * 神クラスを生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ConsoleOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        this.stock = stock;
        this.out = out;
        this.today = today;
    }

    /**
     * コマンド文字列を解釈して処理し、応答の文字列を返します。
     *
     * @param commandLine 受け取ったコマンド文字列
     * @return 応答の文字列（失敗したときは ERROR で始まる）
     */
    @Override
    public String handle(String commandLine) {
        // コマンドの解釈
        String[] parts = commandLine.trim().split("\\s+");
        String command = parts[0];
        // PLACE: 注文を受け付ける
        if (command.equals("PLACE")) {
            // PLACE は「PLACE 注文ID 顧客ID ランク 明細」の5項目で構成される
            if (parts.length != 5) {
                return "ERROR 形式が不正です";
            }
            String orderId = parts[1];
            String customerId = parts[2];
            String rank = parts[3];
            // 会員ランクは3種類だけを受け付ける
            if (!rank.equals("REGULAR") && !rank.equals("SILVER") && !rank.equals("GOLD")) {
                return "ERROR 形式が不正です";
            }
            // 明細は「商品ID:単価:数量」を ; でつないだ形。解釈しながら小計も数える
            List<String[]> lines = new ArrayList<>();
            int subtotal = 0;
            for (String text : parts[4].split(";")) {
                String[] f = text.split(":");
                if (f.length != 3) {
                    return "ERROR 形式が不正です";
                }
                try {
                    int price = Integer.parseInt(f[1]);
                    int qty = Integer.parseInt(f[2]);
                    subtotal += price * qty;
                } catch (NumberFormatException e) {
                    return "ERROR 形式が不正です";
                }
                lines.add(f);
            }
            // 業務ルール: 重複チェック
            if (orderTotal.containsKey(orderId)) {
                return "ERROR 注文IDが重複しています: " + orderId;
            }
            // 業務ルール: 会員ランク割引
            int discount = 0;
            if (rank.equals("GOLD")) {
                discount = subtotal * 10 / 100;
            } else if (rank.equals("SILVER")) {
                discount = subtotal * 5 / 100;
            }
            int total = subtotal - discount;
            // 業務ルール: 在庫の確認
            for (String[] f : lines) {
                if (stock.getOrDefault(f[0], 0) < Integer.parseInt(f[2])) {
                    return "ERROR 在庫が不足しています: " + f[0];
                }
            }
            // 業務ルール: 決済の上限
            if (total > 500000) {
                return "ERROR 決済が拒否されました: " + orderId;
            }
            // データの更新: 在庫と注文の保存
            for (String[] f : lines) {
                stock.put(f[0], stock.get(f[0]) - Integer.parseInt(f[2]));
            }
            orderCustomer.put(orderId, customerId);
            orderTotal.put(orderId, total);
            history.computeIfAbsent(customerId, k -> new ArrayList<>()).add(orderId);
            // 通知（コンソール出力）
            out.println("[NOTIFY] customer=" + customerId + " order=" + orderId + " total=" + total);
            return "OK " + orderId + " total=" + total;
        // SHOW: 注文1件を参照する
        } else if (command.equals("SHOW")) {
            if (parts.length != 2) {
                return "ERROR 形式が不正です";
            }
            if (!orderTotal.containsKey(parts[1])) {
                return "ERROR 注文が見つかりません: " + parts[1];
            }
            return "ORDER " + parts[1] + " customer=" + orderCustomer.get(parts[1]) + " total=" + orderTotal.get(parts[1]);
        // HISTORY: 顧客の注文履歴を参照する
        } else if (command.equals("HISTORY")) {
            if (parts.length != 2) {
                return "ERROR 形式が不正です";
            }
            List<String> ids = history.get(parts[1]);
            // 注文のない顧客は、エラーではなく (none) を返す
            if (ids == null || ids.isEmpty()) {
                return "HISTORY " + parts[1] + " (none)";
            }
            return "HISTORY " + parts[1] + " " + String.join(",", ids);
        }
        // どのコマンドにも当てはまらなかった
        return "ERROR 不明なコマンドです: " + command;
    }
}
