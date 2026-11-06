package com.example.orders.ch18_business_classes;

import java.util.Optional;

/**
 * リポジトリの窓口です。集約（注文と明細）を、まとめて保存し、まとめて取り出します。
 * 明細だけを保存・取得する窓口は作りません。集約のルートだけが、窓口を持ちます。
 */
public interface OrderRepository {

    /**
     * 注文を、明細ごと取り出します。
     *
     * @param orderId 注文ID
     * @return 見つかった注文。なければ空
     */
    Optional<PlacedOrder> find(String orderId);

    /**
     * 注文を、明細ごと保存します。同じIDがあれば、上書きします。
     *
     * @param order 保存する注文
     */
    void save(PlacedOrder order);
}
