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
    private final PlaceOrderHandler commands;
    private final OrderQueryHandler queries;
    private final OrderReadModel readModel;
    private final PrintStream out;
    private final LocalDate today;

    /**
     * 更新側と参照側を組み立てます。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   通知の出力先
     * @param today 今日の日付
     */
    public ExerciseOrderApp(Map<String, Integer> stock, PrintStream out, LocalDate today) {
        this.commands = new PlaceOrderHandler(stock);
        this.readModel = new OrderReadModel();
        this.queries = new OrderQueryHandler(readModel);
        this.out = out;
        this.today = today;
    }

    @Override
    public String handle(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");
        try {
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
            // 業務上の失敗は、例外ではなく ERROR 応答として返す
            return "ERROR " + e.getMessage();
        }
    }

    private void requireArgs(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new OrderValidationException("形式が不正です");
        }
    }

    private String place(String[] parts) {
        // 更新側で注文を処理する
        PlacedOrder placed = commands.place(parsePlace(parts));
        // 参照側のデータを更新する（今は、ここで直接つないでいる）
        readModel.apply(placed.orderId(), placed.customerId(), placed.total());
        out.println("[NOTIFY] customer=" + placed.customerId() + " order=" + placed.orderId() + " total=" + placed.total());
        return "OK " + placed.orderId() + " total=" + placed.total();
    }

    private String show(String orderId) {
        // 参照は、参照側だけを使う
        OrderSummary s = queries.show(orderId);
        return "ORDER " + s.orderId() + " customer=" + s.customerId() + " total=" + s.total();
    }

    private String history(String customerId) {
        List<String> ids = queries.history(customerId);
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
        Customer customer = new Customer(parts[2], parts[2], rank, null, null);
        return new Order(parts[1], customer, lines, null, today);
    }
}
