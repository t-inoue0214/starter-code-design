package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.OrderStatus;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import java.time.LocalDate;
import java.util.List;

/**
 * ファクトリの例です。入力の注文（Order）から、集約（PlacedOrder）を組み立てます。
 * 「新しく作る」と「保存されていたものを復元する」の、2つの作り方を分けて持ちます。
 */
public class PlacedOrderFactory {

    /**
     * 入力の注文から、新しい集約を組み立てます。状態は「注文済み」になります。
     *
     * @param input 入力の注文
     * @return 組み立てた集約
     */
    public PlacedOrder create(Order input) {
        // 明細がないとき
        if (input.lines() == null || input.lines().isEmpty()) {
            // 不正な入力として拒否する
            throw new OrderValidationException("明細は1つ以上必要です");
        }
        // ★重要★ 組み立ての手順は、ここに集める。呼び出す側は、手順を知らなくてよい
        PlacedOrder order = new PlacedOrder(
                input.id(), input.customer().id(), input.orderedOn(), OrderStatus.PLACED);
        // 入力の明細を、1つずつ集約に足す
        for (OrderLine line : input.lines()) {
            // 検証と、同じ商品のまとめは、集約のルートに任せる
            order.addLine(line.productId(), line.unitPrice(), line.quantity());
        }
        // 組み立てた集約を返す
        return order;
    }

    /**
     * 保存されていた内容から、集約を復元します。新規作成と違い、検証や状態の初期化はしません。
     *
     * @param id         保存されていた注文ID
     * @param customerId 保存されていた顧客ID
     * @param orderedOn  保存されていた注文日
     * @param status     保存されていた状態
     * @param lines      保存されていた明細
     * @return 復元した集約
     */
    public PlacedOrder restore(String id, String customerId, LocalDate orderedOn,
                               OrderStatus status, List<PlacedLine> lines) {
        // 保存されていた内容で、集約の本体を作る
        PlacedOrder order = new PlacedOrder(id, customerId, orderedOn, status);
        // 保存されていた明細を、1つずつ戻す
        for (PlacedLine line : lines) {
            // 検証せず、そのまま戻す
            order.restoreLine(line);
        }
        // 復元した集約を返す
        return order;
    }
}
