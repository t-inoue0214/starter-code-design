package com.example.orders.ch10_hexagonal.exercise.adapter.inbound;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderApp;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.domain.OrderResult;
import com.example.orders.ch10_hexagonal.exercise.core.PlacedOrder;
import com.example.orders.ch10_hexagonal.exercise.core.port.inbound.OrderUseCase;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 入口のアダプタです。コンソールのコマンド文字列を解釈し、入口のポート越しに業務の中心へ依頼します。
 */
public class ConsoleCommandAdapter implements OrderApp {
    // 入口のポートを持つフィールド
    private final OrderUseCase useCase;
    // 今日の日付を持つフィールド
    private final LocalDate today;

    /**
     * @param useCase 入口のポート（業務の中心への依頼窓口）
     * @param today   今日の日付
     */
    public ConsoleCommandAdapter(OrderUseCase useCase, LocalDate today) {
        // 引数の入口のポートを、フィールドに保存する
        this.useCase = useCase;
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
        // 空白で区切って、コマンド名と引数に分ける
        String[] parts = commandLine.trim().split("\\s+");
        // 例外が起こりうる処理を、ここから試す
        try {
            // 最初の語がコマンド名。コマンドごとの処理へ振り分ける
            switch (parts[0]) {
                // PLACE コマンドのとき
                case "PLACE":
                    // PLACE の処理を呼び、その応答を返す
                    return place(parts);
                // SHOW コマンドのとき
                case "SHOW":
                    // 語の数が2つであることを確かめる
                    requireArgs(parts, 2);
                    // SHOW の処理を呼び、その応答を返す
                    return show(parts[1]);
                // HISTORY コマンドのとき
                case "HISTORY":
                    // 語の数が2つであることを確かめる
                    requireArgs(parts, 2);
                    // HISTORY の処理を呼び、その応答を返す
                    return history(parts[1]);
                // どのコマンドにも当てはまらないとき
                default:
                    // 未知のコマンドを、検証エラーとして投げる
                    throw new OrderValidationException("不明なコマンドです: " + parts[0]);
            }
        // 業務上の失敗（検証エラー、在庫不足、決済拒否）が起きたとき
        } catch (OrderValidationException | OutOfStockException | PaymentDeclinedException e) {
            // ★重要★ 業務上の失敗は、例外ではなく ERROR 応答として返す
            return "ERROR " + e.getMessage();
        }
    }

    /**
     * コマンドの語の数が期待どおりかを確かめます。違えば、形式の誤りとして扱います。
     *
     * @param parts    空白で区切ったコマンドの語
     * @param expected 期待する語の数
     */
    private void requireArgs(String[] parts, int expected) {
        // 引数の数が違うコマンドは、形式の誤りとして扱う
        if (parts.length != expected) {
            // 形式の誤りを、検証エラーとして投げる
            throw new OrderValidationException("形式が不正です");
        }
    }

    /**
     * PLACE コマンドを処理し、応答を組み立てます。
     *
     * @param parts 空白で区切ったコマンドの語
     * @return 応答の文字列
     */
    private String place(String[] parts) {
        // ★重要★ 入口のポート（OrderUseCase）越しに、中心を呼ぶ
        OrderResult result = useCase.place(parsePlace(parts));
        // 成功の応答として、注文IDと請求額を返す
        return "OK " + result.orderId() + " total=" + result.total();
    }

    /**
     * SHOW コマンドの応答を組み立てます。
     *
     * @param orderId 注文ID
     * @return 応答の文字列
     */
    private String show(String orderId) {
        // 入口のポート越しに、注文を探させる
        PlacedOrder p = useCase.find(orderId);
        // 注文の内容を、ORDER 応答の文字列にして返す
        return "ORDER " + p.orderId() + " customer=" + p.customerId() + " total=" + p.total();
    }

    /**
     * HISTORY コマンドの応答を組み立てます。
     *
     * @param customerId 顧客ID
     * @return 応答の文字列
     */
    private String history(String customerId) {
        // 入口のポートから、顧客の注文ID一覧を取り出す
        List<String> ids = useCase.history(customerId);
        // 履歴が空なら (none) と表示する
        return "HISTORY " + customerId + " " + (ids.isEmpty() ? "(none)" : String.join(",", ids));
    }

    /** PLACE コマンドを注文に変換します。形式が不正なら OrderValidationException です。 */
    private Order parsePlace(String[] parts) {
        // 形式の確認（項目数）
        if (parts.length != 5) {
            // 形式の誤りを、検証エラーとして投げる
            throw new OrderValidationException("形式が不正です");
        }
        // 会員ランクの解釈
        MemberRank rank;
        // 例外が起こりうる処理を、ここから試す
        try {
            // 会員ランクの文字列を、列挙型に直す
            rank = MemberRank.valueOf(parts[3]);
        // 列挙型に直せなかったとき
        } catch (IllegalArgumentException e) {
            // 形式の誤りを、検証エラーとして投げる
            throw new OrderValidationException("形式が不正です");
        }
        // 明細の解釈（商品ID:単価:数量 を ; でつないだもの）
        List<OrderLine> lines = new ArrayList<>();
        // 明細の文字列を、; で1件ずつに分けて処理する
        for (String text : parts[4].split(";")) {
            // 明細1件を、: で商品ID・単価・数量に分ける
            String[] f = text.split(":");
            // 明細は、商品ID・単価・数量の3項目のはず
            if (f.length != 3) {
                // 形式の誤りを、検証エラーとして投げる
                throw new OrderValidationException("形式が不正です");
            }
            // 例外が起こりうる処理を、ここから試す
            try {
                // 商品ID・単価・数量から明細を作り、一覧に加える
                lines.add(new OrderLine(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2])));
            // 数値に直せなかったとき
            } catch (NumberFormatException e) {
                // 形式の誤りを、検証エラーとして投げる
                throw new OrderValidationException("形式が不正です");
            }
        }
        // コマンドには顧客名も連絡先もないので、顧客IDを名前の代わりにし、連絡先は空にする
        Customer customer = new Customer(parts[2], parts[2], rank, null, null);
        // クーポンなしの注文として組み立てる。日付は、アプリが持つ今日の日付を使う
        return new Order(parts[1], customer, lines, null, today);
    }
}
