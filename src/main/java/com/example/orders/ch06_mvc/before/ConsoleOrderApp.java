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
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
        // 引数の今日の日付を、フィールドに保存する
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
        // 最初の語を、コマンド名として取り出す
        String command = parts[0];
        // PLACE: 注文を受け付ける
        if (command.equals("PLACE")) {
            // PLACE は「PLACE 注文ID 顧客ID ランク 明細」の5項目で構成される
            if (parts.length != 5) {
                // 形式の誤りを、ERROR で始まる応答として返す
                return "ERROR 形式が不正です";
            }
            // 2番目の語を、注文IDとして取り出す
            String orderId = parts[1];
            // 3番目の語を、顧客IDとして取り出す
            String customerId = parts[2];
            // 4番目の語を、会員ランクの文字列として取り出す
            String rank = parts[3];
            // 会員ランクは3種類だけを受け付ける
            if (!rank.equals("REGULAR") && !rank.equals("SILVER") && !rank.equals("GOLD")) {
                // 形式の誤りを、ERROR で始まる応答として返す
                return "ERROR 形式が不正です";
            }
            // 明細は「商品ID:単価:数量」を ; でつないだ形。解釈しながら小計も数える
            List<String[]> lines = new ArrayList<>();
            // 小計を0から数え始める
            int subtotal = 0;
            // 明細の文字列を、; で1件ずつに分けて処理する
            for (String text : parts[4].split(";")) {
                // 明細1件を、: で商品ID・単価・数量に分ける
                String[] f = text.split(":");
                // 明細は、商品ID・単価・数量の3項目のはず
                if (f.length != 3) {
                    // 形式の誤りを、ERROR で始まる応答として返す
                    return "ERROR 形式が不正です";
                }
                // 例外が起こりうる処理を、ここから試す
                try {
                    // 単価の文字列を、数値に直す
                    int price = Integer.parseInt(f[1]);
                    // 数量の文字列を、数値に直す
                    int qty = Integer.parseInt(f[2]);
                    // 単価 × 数量を、小計に加える
                    subtotal += price * qty;
                // 数値に直せなかったとき
                } catch (NumberFormatException e) {
                    // 形式の誤りを、ERROR で始まる応答として返す
                    return "ERROR 形式が不正です";
                }
                // 解釈した明細を、あとの処理のために覚えておく
                lines.add(f);
            }
            // 業務ルール: 重複チェック
            if (orderTotal.containsKey(orderId)) {
                // 注文IDの重複を、ERROR 応答として返す
                return "ERROR 注文IDが重複しています: " + orderId;
            }
            // 業務ルール: 会員ランク割引
            int discount = 0;
            // ゴールド会員なら
            if (rank.equals("GOLD")) {
                // ゴールド会員の割引額は、小計の10%
                discount = subtotal * 10 / 100;
            // シルバー会員なら
            } else if (rank.equals("SILVER")) {
                // シルバー会員の割引額は、小計の5%
                discount = subtotal * 5 / 100;
            }
            // 請求額を、小計から割引額を引いて求める
            int total = subtotal - discount;
            // 業務ルール: 在庫の確認
            for (String[] f : lines) {
                // 在庫が、注文の数量に足りないとき
                if (stock.getOrDefault(f[0], 0) < Integer.parseInt(f[2])) {
                    // 在庫不足を、ERROR 応答として返す
                    return "ERROR 在庫が不足しています: " + f[0];
                }
            }
            // 業務ルール: 決済の上限
            if (total > 500000) {
                // 決済拒否を、ERROR 応答として返す
                return "ERROR 決済が拒否されました: " + orderId;
            }
            // データの更新: 在庫と注文の保存
            for (String[] f : lines) {
                // 商品の在庫を、注文の数量だけ減らす
                stock.put(f[0], stock.get(f[0]) - Integer.parseInt(f[2]));
            }
            // 注文IDと顧客IDの対応を保存する
            orderCustomer.put(orderId, customerId);
            // 注文IDと請求額の対応を保存する
            orderTotal.put(orderId, total);
            // 顧客の履歴の末尾に、この注文IDを追加する（履歴がなければ作る）
            history.computeIfAbsent(customerId, k -> new ArrayList<>()).add(orderId);
            // 通知（コンソール出力）
            out.println("[NOTIFY] customer=" + customerId + " order=" + orderId + " total=" + total);
            // 成功の応答として、注文IDと請求額を返す
            return "OK " + orderId + " total=" + total;
        // SHOW: 注文1件を参照する
        } else if (command.equals("SHOW")) {
            // SHOW と HISTORY は、コマンド名と引数の2語のはず
            if (parts.length != 2) {
                // 形式の誤りを、ERROR で始まる応答として返す
                return "ERROR 形式が不正です";
            }
            // その注文IDの注文が、保存されていないとき
            if (!orderTotal.containsKey(parts[1])) {
                // 注文が見つからないことを、ERROR 応答として返す
                return "ERROR 注文が見つかりません: " + parts[1];
            }
            // 注文の内容（顧客IDと請求額）を、応答の文字列にして返す
            return "ORDER " + parts[1] + " customer=" + orderCustomer.get(parts[1]) + " total=" + orderTotal.get(parts[1]);
        // HISTORY: 顧客の注文履歴を参照する
        } else if (command.equals("HISTORY")) {
            // SHOW と HISTORY は、コマンド名と引数の2語のはず
            if (parts.length != 2) {
                // 形式の誤りを、ERROR で始まる応答として返す
                return "ERROR 形式が不正です";
            }
            // 指定の顧客の注文ID一覧を取り出す
            List<String> ids = history.get(parts[1]);
            // 注文のない顧客は、エラーではなく (none) を返す
            if (ids == null || ids.isEmpty()) {
                // 履歴がないことを、(none) で表す
                return "HISTORY " + parts[1] + " (none)";
            }
            // 注文IDを , でつないで、履歴の応答にする
            return "HISTORY " + parts[1] + " " + String.join(",", ids);
        }
        // どのコマンドにも当てはまらなかった
        return "ERROR 不明なコマンドです: " + command;
    }
}
