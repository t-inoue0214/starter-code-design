package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.InventoryItem;
import com.example.orders.domain.OutOfStockException;
import java.util.Map;

/**
 * ドメインサービスの例です。「注文の全明細を、複数の在庫から引き当てる」ルールを扱います。
 * 注文にも在庫にも属さない、複数の概念にまたがるルールなので、どちらかに入れずに独立させます。
 * 自分では状態を持たず、保存も通信もしません。
 */
public class StockAllocationService {

    /**
     * 注文の全明細を引き当てます。1つでも足りなければ、どの在庫も変更しません。
     *
     * @param order 引き当てる注文
     * @param items 商品IDをキーにした在庫
     * @throws OutOfStockException 在庫がない、または足りない明細があるとき
     */
    public void allocate(PlacedOrder order, Map<String, InventoryItem> items) {
        // 全明細を、変更する前に1つずつ確かめる
        for (PlacedLine line : order.lines()) {
            // 対応する在庫を取り出す
            InventoryItem item = items.get(line.productId());
            // 在庫がないか、数量が足りないとき
            if (item == null || item.quantity() < line.quantity()) {
                // ★重要★ 1つでも足りなければ、ここで止める。まだ何も変更していない
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 全部足りると分かったので、1つずつ引き当てる
        for (PlacedLine line : order.lines()) {
            // 在庫の減らし方は、在庫（Entity）自身に任せる
            items.get(line.productId()).reserve(line.quantity());
        }
    }
}
