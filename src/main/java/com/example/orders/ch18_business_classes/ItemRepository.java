package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.InventoryItem;
import java.util.Collection;
import java.util.Map;

/**
 * 在庫リポジトリの窓口です。在庫（Entity）の保存先を隠します。
 */
public interface ItemRepository {

    /**
     * 指定した商品の在庫を、まとめて取り出します。
     *
     * @param productIds 商品IDの一覧
     * @return 商品IDをキーにした在庫。存在しない商品は含まれない
     */
    Map<String, InventoryItem> findByIds(Collection<String> productIds);

    /**
     * 在庫を保存します。
     *
     * @param item 保存する在庫
     */
    void save(InventoryItem item);
}
