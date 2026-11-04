package com.example.orders.ch11_cqrs.exercise;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderApp;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch11_cqrs.exercise.command.PlaceOrderHandler;
import com.example.orders.ch11_cqrs.exercise.command.PlacedOrder;
import com.example.orders.ch11_cqrs.exercise.query.OrderQueryHandler;
import com.example.orders.ch11_cqrs.exercise.query.OrderReadModel;
import com.example.orders.ch11_cqrs.exercise.query.OrderSummary;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 第11章の解答例です。更新側（command）と参照側（query）を組み立て、コマンドを振り分けます。
 */
public class ExerciseOrderApp implements OrderApp {
    // 更新側を持つフィールド
    private final PlaceOrderHandler commands;
    // 参照側を持つフィールド
    private final OrderQueryHandler queries;
    // 読み取りモデルを持つフィールド
    private final OrderReadModel readModel;
    // 出力先を持つフィールド
    private final PrintStream out;
    // 今日の日付を持つフィールド
    private final LocalDate today;

    /**
     * 更新側と参照側を組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        // ★重要★ 更新側を作る（更新側は参照側を知らない）
        this.commands = new PlaceOrderHandler(stock);
        // ★重要★ 参照用の読み取りモデルを作る（更新用の状態とは別に持つ）
        this.readModel = new OrderReadModel();
        // 読み取りモデルを使う参照側を作って、保存する
        this.queries = new OrderQueryHandler(readModel);
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
        // ★重要★ 更新側で注文を処理する
        PlacedOrder placed = commands.place(parsePlace(parts));
        // ★重要★ 参照側のデータを更新する（今は、ここで直接つないでいる）
        readModel.apply(placed.orderId(), placed.customerId(), placed.total());
        // 成立した注文を、通知として出力する
        out.println(
                "[NOTIFY] customer=" + placed.customerId() + " order=" + placed.orderId() + " total=" + placed.total());
        // 成功の応答として、注文IDと請求額を返す
        return "OK " + placed.orderId() + " total=" + placed.total();
    }

    /**
     * SHOW コマンドの応答を組み立てます。
     *
     * @param orderId 注文ID
     * @return 応答の文字列
     */
    private String show(String orderId) {
        // ★重要★ 参照は、参照側だけを使う
        OrderSummary s = queries.show(orderId);
        // 注文の概要を、ORDER 応答の文字列にして返す
        return "ORDER " + s.orderId() + " customer=" + s.customerId() + " total=" + s.total();
    }

    /**
     * HISTORY コマンドの応答を組み立てます。
     *
     * @param customerId 顧客ID
     * @return 応答の文字列
     */
    private String history(String customerId) {
        // 参照側から、顧客の注文ID一覧を取り出す
        List<String> ids = queries.history(customerId);
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
