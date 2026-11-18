package com.example.orders.ch21_failures.before;

import com.example.orders.ch21_failures.PaymentClient;
import java.util.HashMap;
import java.util.Map;

/**
 * 失敗をすべて -1 で返す注文処理です。失敗の理由も、プログラムの誤りも、区別できません。
 * どんな例外もまとめて捕まえて、なかったことにしています。
 */
public class SilentOrderService {
    // 商品IDごとの単価を持つフィールド
    private final Map<String, Integer> prices = new HashMap<>();
    // 商品IDごとの在庫の数を持つフィールド
    private final Map<String, Integer> stocks = new HashMap<>();
    // 決済会社のライブラリを持つフィールド
    private final PaymentClient client;

    /**
     * 注文処理を作ります。
     *
     * @param client 決済会社のライブラリ
     */
    public SilentOrderService(PaymentClient client) {
        // 引数のライブラリを、フィールドに保存する
        this.client = client;
    }

    /**
     * 商品を登録します。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param stock     在庫の数
     */
    public void register(String productId, int unitPrice, int stock) {
        // 単価を登録する
        prices.put(productId, unitPrice);
        // 在庫の数を登録する
        stocks.put(productId, stock);
    }

    /**
     * 注文を受け付けます。
     *
     * @param customerId   顧客ID
     * @param productId    商品ID
     * @param quantityText 数量（文字列のまま）
     * @return 合計金額。失敗したときは、理由にかかわらず -1
     */
    public int place(String customerId, String productId, String quantityText) {
        // 例外が起こりうる処理を、ここから試す
        try {
            // 顧客IDの空白を取り除く（null なら、ここで例外になる）
            String customer = customerId.trim();
            // 数量を整数に変える（数字でなければ、ここで例外になる）
            int quantity = Integer.parseInt(quantityText);
            // 単価を探す（商品がなければ、取り出すときに例外になる）
            int unitPrice = prices.get(productId);
            // 在庫が足りないとき
            if (stocks.get(productId) < quantity) {
                // 失敗の印を返す
                return -1;
            }
            // 合計を計算する
            int total = unitPrice * quantity;
            // 決済が断られたとき（通信の失敗は例外になる）
            if (!client.authorize(customer, total)) {
                // 失敗の印を返す
                return -1;
            }
            // 在庫を減らす
            stocks.put(productId, stocks.get(productId) - quantity);
            // 合計を返す
            return total;
        // どんな例外が起きても
        } catch (Exception e) {
            // 理由を捨てて、失敗の印だけを返す（原因の調査ができなくなる）
            return -1;
        }
    }
}
