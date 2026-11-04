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
    // 商品IDごとの在庫数を持つフィールド
    private final Stock stock;
    // 注文の台帳を持つフィールド
    private final OrderBook book;

    /**
     * @param stock 在庫
     * @param book  注文の台帳
     */
    public OrderControl(Stock stock, OrderBook book) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の注文の台帳を、フィールドに保存する
        this.book = book;
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 注文
     * @return 成立した注文の概要
     */
    public OrderSummary place(Order order) {
        // ★重要★ 重複の判断は、台帳（Entity）に尋ねる
        if (book.contains(order.id())) {
            // 注文IDの重複を、検証エラーとして投げる
            throw new OrderValidationException("注文IDが重複しています: " + order.id());
        }
        // ★重要★ 在庫が足りるかの判断は、在庫（Entity）に任せる
        stock.assertAvailable(order.lines());
        // ★重要★ 金額の計算と決済上限のルールは、Entity（PlacedOrder）に任せる
        PlacedOrder placed = PlacedOrder.from(order);
        // 在庫（Entity）に、全明細を引き当てさせる
        stock.reserve(order.lines());
        // 台帳に、成立した注文を登録する
        book.add(placed);
        // 成立した注文から概要を作って返す
        return summaryOf(placed);
    }

    /**
     * @param orderId 注文ID
     * @return 注文の概要
     */
    public OrderSummary find(String orderId) {
        // 台帳から、成立した注文を取り出す
        PlacedOrder placed = book.find(orderId);
        // 注文が見つからないとき
        if (placed == null) {
            // 注文が見つからないことを、検証エラーとして投げる
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // 成立した注文から概要を作って返す
        return summaryOf(placed);
    }

    /**
     * @param customerId 顧客ID
     * @return 顧客の注文IDの一覧
     */
    public List<String> history(String customerId) {
        // 台帳から、顧客の注文ID一覧を取り出して返す
        return book.idsOf(customerId);
    }

    /**
     * 成立した注文から、呼び出し側に返す概要を作ります。
     *
     * @param placed 成立した注文
     * @return 注文の概要
     */
    private OrderSummary summaryOf(PlacedOrder placed) {
        // ★重要★ Entity を直接渡さず、必要な値だけを詰めた概要にして返す
        return new OrderSummary(placed.orderId(), placed.customerId(), placed.total());
    }
}
