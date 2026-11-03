package com.example.orders.ch09_mvc.exercise.controller;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderApp;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.ch09_mvc.exercise.model.OrderModel;
import com.example.orders.ch09_mvc.exercise.model.OrderRecord;
import com.example.orders.ch09_mvc.exercise.view.OrderView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 入力を解釈して Model に渡し、結果を View に渡します（Controller）。業務ルールも、出力の形も持ちません。
 */
public class OrderController implements OrderApp {
    private final OrderModel model;
    private final OrderView view;
    private final LocalDate today;

    /**
     * @param model Model
     * @param view  View
     * @param today 今日の日付
     */
    public OrderController(OrderModel model, OrderView view, LocalDate today) {
        this.model = model;
        this.view = view;
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
            return view.error(e.getMessage());
        }
    }

    private void requireArgs(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new OrderValidationException("形式が不正です");
        }
    }

    private String place(String[] parts) {
        // 入力を Model に渡し、結果を View に渡す（Controller は仲介だけをする）
        OrderRecord record = model.place(parsePlace(parts));
        view.notifyPlaced(record);
        return view.ok(record);
    }

    private String show(String orderId) {
        return view.order(model.find(orderId));
    }

    private String history(String customerId) {
        return view.history(customerId, model.history(customerId));
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
