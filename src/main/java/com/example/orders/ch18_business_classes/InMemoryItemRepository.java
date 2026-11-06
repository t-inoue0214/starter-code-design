package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.InventoryItem;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在庫リポジトリの、メモリ上の実装です。取り出す在庫は、保存したものとは別のコピーです。
 * このため、保存し直すまでは、取り出した在庫を変更しても保存内容は変わりません。
 */
public class InMemoryItemRepository implements ItemRepository {
    // 保存した在庫の数量を、商品IDごとに持つフィールド
    private final Map<String, Integer> quantities = new LinkedHashMap<>();

    /**
     * 在庫を登録します。テストや初期データの準備に使います。
     *
     * @param productId 商品ID
     * @param quantity  在庫数
     */
    public void add(String productId, int quantity) {
        // 商品IDと数量を、保存する
        quantities.put(productId, quantity);
    }

    /**
     * 保存されている在庫数を返します。確認用です。
     *
     * @param productId 商品ID
     * @return 保存されている在庫数
     */
    public int quantityOf(String productId) {
        // 保存されている数量を返す
        return quantities.get(productId);
    }

    /**
     * 指定した商品の在庫を、コピーとして取り出します。
     *
     * @param productIds 商品IDの一覧
     * @return 商品IDをキーにした在庫のコピー
     */
    @Override
    public Map<String, InventoryItem> findByIds(Collection<String> productIds) {
        // 結果を入れる入れ物を用意する
        Map<String, InventoryItem> found = new LinkedHashMap<>();
        // 指定された商品IDを、1つずつ調べる
        for (String productId : productIds) {
            // 保存されている商品のとき
            if (quantities.containsKey(productId)) {
                // 保存内容と切り離すため、新しい在庫として作り直す
                found.put(productId, new InventoryItem(productId, quantities.get(productId)));
            }
        }
        // 見つかった在庫を返す
        return found;
    }

    /**
     * 在庫の数量を保存します。
     *
     * @param item 保存する在庫
     */
    @Override
    public void save(InventoryItem item) {
        // 商品IDと現在の数量を、保存する
        quantities.put(item.productId(), item.quantity());
    }
}
