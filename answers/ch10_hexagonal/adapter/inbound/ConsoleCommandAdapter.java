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
    private final OrderUseCase useCase;
    private final LocalDate today;

    /**
     * @param useCase 入口のポート（業務の中心への依頼窓口）
     * @param today   今日の日付
     */
    public ConsoleCommandAdapter(OrderUseCase useCase, LocalDate today) {
        this.useCase = useCase;
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
        OrderResult result = useCase.place(parsePlace(parts));
        return "OK " + result.orderId() + " total=" + result.total();
    }

    private String show(String orderId) {
        PlacedOrder p = useCase.find(orderId);
        return "ORDER " + p.orderId() + " customer=" + p.customerId() + " total=" + p.total();
    }

    private String history(String customerId) {
        List<String> ids = useCase.history(customerId);
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
