package com.example.orders.ch05_ecb.exercise.control;

import com.example.orders.ch05_ecb.exercise.entity.OrderBook;
import com.example.orders.ch05_ecb.exercise.entity.PlacedOrder;
import com.example.orders.ch05_ecb.exercise.entity.Stock;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderValidationException;
import java.util.List;

/**
 * 注文に関する処理の流れを組み立てます（Control）。
 * ルールそのものは Entity に任せ、「どの順で、どの Entity を使うか」だけを知っています。
 */
public class OrderControl {
    private final Stock stock;
    private final OrderBook book;

    /**
     * @param stock 在庫
     * @param book  注文の台帳
     */
    public OrderControl(Stock stock, OrderBook book) {
        this.stock = stock;
        this.book = book;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 成立した注文の概要
     */
    public OrderSummary place(Order order) {
        if (book.contains(order.id())) {
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        stock.assertAvailable(order.lines());
        PlacedOrder placed = PlacedOrder.from(order);
        stock.reserve(order.lines());
        book.add(placed);
        return summaryOf(placed);
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary find(String orderId) {
        PlacedOrder placed = book.find(orderId);
        if (placed == null) {
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        return summaryOf(placed);
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        return book.idsOf(customerId);
    }

    /**
     * 成立した注文から、呼び出し側に返す概要を作ります。
     *
     * @param placed 成立した注文
     * @return 注文の概要
     */
    private OrderSummary summaryOf(PlacedOrder placed) {
        return new OrderSummary(placed.orderId(), placed.customerId(), placed.total());
    }
}
