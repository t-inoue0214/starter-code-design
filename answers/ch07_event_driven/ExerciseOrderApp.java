package com.example.orders.ch07_event_driven.exercise;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderApp;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.domain.OrderResult;
import com.example.orders.ch07_event_driven.exercise.event.EventBus;
import com.example.orders.ch07_event_driven.exercise.event.SimpleEventBus;
import com.example.orders.ch07_event_driven.exercise.order.PlaceOrderHandler;
import com.example.orders.ch07_event_driven.exercise.query.OrderQueryHandler;
import com.example.orders.ch07_event_driven.exercise.query.OrderReadModel;
import com.example.orders.ch07_event_driven.exercise.query.OrderSummary;
import com.example.orders.ch07_event_driven.exercise.subscriber.NotificationSubscriber;
import com.example.orders.ch07_event_driven.exercise.subscriber.ReadModelSubscriber;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 第7章の解答例です。イベントバスを介して、出す側と受け取る側を組み立てます。
 */
public class ExerciseOrderApp implements OrderApp {
    private final PlaceOrderHandler commands;
    private final OrderQueryHandler queries;
    private final LocalDate today;

    /**
     * 標準のイベントバスで、アプリを組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        this(stock, out, today, new SimpleEventBus());
    }

    /**
     * イベントバスを外から受け取って、アプリを組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     * @param bus   イベントバス
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today, EventBus bus) {
        OrderReadModel readModel = new OrderReadModel();
        this.queries = new OrderQueryHandler(readModel);
        // ★重要★ 受け取る側を、バスに登録する（出す側は、これらを知らない）
        new NotificationSubscriber(out).register(bus);
        new ReadModelSubscriber(readModel).register(bus);
        this.commands = new PlaceOrderHandler(stock, bus);
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
        try {
            // 最初の語がコマンド名。コマンドごとの処理へ振り分ける
            switch (parts[0]) {
                case "PLACE":
                    return place(parts);
                case "SHOW":
                    requireArgs(parts, 2);
                    return show(parts[1]);
                case "HISTORY":
                    requireArgs(parts, 2);
                    return history(parts[1]);
                default:
                    throw new OrderValidationException("不明なコマンドです: " + parts[0]);
            }
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
        // ★重要★ イベントを出すのは更新側。通知や参照データの更新は、バスに登録された受け取り手が行う
        OrderResult result = commands.place(parsePlace(parts));
        return "OK " + result.orderId() + " total=" + result.total();
    }

    /**
     * SHOW コマンドの応答を組み立てます。
     *
     * @param orderId 注文ID
     * @return 応答の文字列
     */
    private String show(String orderId) {
        OrderSummary s = queries.show(orderId);
        return "ORDER " + s.orderId() + " customer=" + s.customerId() + " total=" + s.total();
    }

    /**
     * HISTORY コマンドの応答を組み立てます。
     *
     * @param customerId 顧客ID
     * @return 応答の文字列
     */
    private String history(String customerId) {
        List<String> ids = queries.history(customerId);
        // 履歴が空なら (none) と表示する
        return "HISTORY " + customerId + " " + (ids.isEmpty() ? "(none)" : String.join(",", ids));
    }

    /** PLACE コマンドを注文に変換します。形式が不正なら OrderValidationException です。 */
    private Order parsePlace(String[] parts) {
        // 形式の確認（項目数）
        if (parts.length != 5) {
            throw new OrderValidationException("形式が不正です");
        }
        // 会員ランクの解釈
        MemberRank rank;
        try {
            rank = MemberRank.valueOf(parts[3]);
        } catch (IllegalArgumentException e) {
            throw new OrderValidationException("形式が不正です");
        }
        // 明細の解釈（商品ID:単価:数量 を ; でつないだもの）
        List<OrderLine> lines = new ArrayList<>();
        for (String text : parts[4].split(";")) {
            String[] f = text.split(":");
            if (f.length != 3) {
                throw new OrderValidationException("形式が不正です");
            }
            try {
                lines.add(new OrderLine(f[0], Integer.parseInt(f[1]), Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                throw new OrderValidationException("形式が不正です");
            }
        }
        // コマンドには顧客名も連絡先もないので、顧客IDを名前の代わりにし、連絡先は空にする
        Customer customer = new Customer(parts[2], parts[2], rank, null, null);
        // クーポンなしの注文として組み立てる。日付は、アプリが持つ今日の日付を使う
        return new Order(parts[1], customer, lines, null, today);
    }
}
