package com.example.orders.ch20_visibility.app;

import com.example.orders.ch20_visibility.order.Order;

/**
 * 注文のパッケージの外から、注文を使うサービスです。
 * 使えるのは Order の public な操作だけなので、使う側は中の手順を知らなくて済みます。
 */
public final class CheckoutService {

    /**
     * 商品を1つ入れた注文を作り、確定して返します。
     *
     * @param customerId 顧客ID
     * @param productId  商品ID
     * @param unitPrice  単価（円）
     * @param quantity   数量
     * @return 確定した注文
     */
    public Order checkout(String customerId, String productId, int unitPrice, int quantity) {
        // 公開された作り方で、注文を作る
        Order order = Order.create(customerId);
        // ★重要★ 外からは、公開された操作の順に呼ぶだけ。合計の計算し直しなどの手順を知らなくてよい
        order.addItem(productId, unitPrice, quantity);
        // 注文を確定する
        order.place();
        // 確定した注文を返す
        return order;
    }
}
