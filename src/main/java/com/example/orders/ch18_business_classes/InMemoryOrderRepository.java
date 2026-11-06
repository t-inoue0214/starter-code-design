package com.example.orders.ch18_business_classes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 注文リポジトリの、メモリ上の実装です。保存先がデータベースに替わっても、窓口は変わりません。
 */
public class InMemoryOrderRepository implements OrderRepository {
    // 保存した注文を、IDごとに持つフィールド
    private final Map<String, PlacedOrder> store = new LinkedHashMap<>();
    // 保存と復元に使うファクトリを持つフィールド
    private final PlacedOrderFactory factory = new PlacedOrderFactory();

    /**
     * 注文を、明細ごと取り出します。保存したものとは別のコピーを返します。
     *
     * @param orderId 注文ID
     * @return 見つかった注文。なければ空
     */
    @Override
    public Optional<PlacedOrder> find(String orderId) {
        // 保存されている注文を探す
        PlacedOrder saved = store.get(orderId);
        // 見つからないとき
        if (saved == null) {
            // 空を返す
            return Optional.empty();
        }
        // ★重要★ 保存したものを直接渡さず、復元したコピーを渡す（保存し直すまで保存内容は変わらない）
        return Optional.of(factory.restore(
                saved.id(), saved.customerId(), saved.orderedOn(), saved.status(), saved.lines()));
    }

    /**
     * 注文を、明細ごと保存します。
     *
     * @param order 保存する注文
     */
    @Override
    public void save(PlacedOrder order) {
        // 渡された内容をコピーして、IDごとに保存する
        store.put(order.id(), factory.restore(
                order.id(), order.customerId(), order.orderedOn(), order.status(), order.lines()));
    }
}
