package com.example.orders.ch08_layered.exercise.presentation;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderApp;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import com.example.orders.domain.OrderResult;
import com.example.orders.ch08_layered.exercise.service.OrderService;
import com.example.orders.ch08_layered.exercise.service.OrderSummary;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * コマンド文字列の解釈と、応答の組み立てを行います（表示層）。業務ルールは持たず、業務層に任せます。
 */
public class OrderCommandController implements OrderApp {
    private final OrderService service;
    private final LocalDate today;

    /**
     * @param service 業務層
     * @param today   今日の日付
     */
    public OrderCommandController(OrderService service, LocalDate today) {
        this.service = service;
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
        OrderResult result = service.place(parsePlace(parts));
        return "OK " + result.orderId() + " total=" + result.total();
    }

    private String show(String orderId) {
        OrderSummary summary = service.find(orderId);
        return "ORDER " + summary.orderId() + " customer=" + summary.customerId() + " total=" + summary.total();
    }

    private String history(String customerId) {
        List<String> ids = service.history(customerId);
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
