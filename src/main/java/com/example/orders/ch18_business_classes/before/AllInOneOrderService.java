package com.example.orders.ch18_business_classes.before;

import com.example.orders.ch18_business_classes.PaymentGateway;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分ける前の実装です。「注文する」「キャンセルする」の中に、流れも、ルールも、計算も、
 * 保存も、外部サービスの呼び出しも、すべて入っています。動作は正しく、テストも通ります。
 * 問題は、種類の違うものが1つのクラスに混ざり、変更の入れ先が分かりにくいことです。
 */
public class AllInOneOrderService {
    // 在庫数を、商品IDごとに持つフィールド（保存先そのもの）
    private final Map<String, Integer> stock = new LinkedHashMap<>();
    // 注文者を、注文IDごとに持つフィールド（保存先そのもの）
    private final Map<String, String> ownerByOrder = new LinkedHashMap<>();
    // 注文の状態を、注文IDごとに持つフィールド（保存先そのもの）
    private final Map<String, String> statusByOrder = new LinkedHashMap<>();
    // 決済の窓口を持つフィールド
    private final PaymentGateway gateway;

    /**
     * サービスを作ります。
     *
     * @param gateway 決済の窓口
     */
    public AllInOneOrderService(PaymentGateway gateway) {
        // 引数の決済の窓口を、フィールドに保存する
        this.gateway = gateway;
    }

    /**
     * 在庫を登録します。
     *
     * @param productId 商品ID
     * @param quantity  在庫数
     */
    public void addStock(String productId, int quantity) {
        // 商品IDと数量を、保存する
        stock.put(productId, quantity);
    }

    /**
     * 在庫数を返します。確認用です。
     *
     * @param productId 商品ID
     * @return 在庫数
     */
    public int stockOf(String productId) {
        // 保存されている数量を返す
        return stock.get(productId);
    }

    /**
     * 注文を受け付けます。
     *
     * @param order 入力の注文
     * @return 請求額（税込、円）
     */
    public int place(Order order) {
        // 【ファクトリの仕事】明細がないとき、拒否する
        if (order.lines() == null || order.lines().isEmpty()) {
            // 不正な入力として拒否する
            throw new OrderValidationException("明細は1つ以上必要です");
        }
        // 【集約の仕事】明細を数えながら、金額を合計する
        int subtotal = 0;
        // 明細を1つずつ調べる
        for (OrderLine line : order.lines()) {
            // 【集約の仕事】数量が範囲外のとき、拒否する
            if (line.quantity() < 1 || line.quantity() > 99) {
                // 不正な入力として拒否する
                throw new OrderValidationException("数量が範囲外です: " + line.productId());
            }
            // 明細の金額を、小計に足す
            subtotal += line.unitPrice() * line.quantity();
        }
        // 【計算クラスの仕事】税率を決めて、税額を計算する（法律の変更がここに混ざる）
        int percent = order.orderedOn().isBefore(java.time.LocalDate.of(2019, 10, 1)) ? 8 : 10;
        // 請求額を、小計と税額から求める
        int total = subtotal + subtotal * percent / 100;
        // 【ポリシーの仕事】決済額の上限を確かめる（上限の値がここに埋まっている）
        if (total > 300000) {
            // 上限を超えたので、決済の拒否として扱う
            throw new PaymentDeclinedException("決済額が上限を超えています: " + total);
        }
        // 【ドメインサービスの仕事】全明細の在庫を、先に確かめる
        for (OrderLine line : order.lines()) {
            // 在庫がないか、足りないとき
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                // 在庫不足として拒否する
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 【ゲートウェイの仕事】外部の決済サービスへ請求する
        gateway.charge(order.customer().id(), total);
        // 【エンティティの仕事】在庫を減らす
        for (OrderLine line : order.lines()) {
            // 在庫の数量を、直接書き換える
            stock.merge(line.productId(), -line.quantity(), Integer::sum);
        }
        // 【リポジトリの仕事】注文者を保存する
        ownerByOrder.put(order.id(), order.customer().id());
        // 【リポジトリの仕事】状態を保存する
        statusByOrder.put(order.id(), "PLACED");
        // 請求額を返す
        return total;
    }

    /**
     * 注文をキャンセルします。
     *
     * @param orderId     注文ID
     * @param requesterId 操作する人のID
     * @param admin       運用担当者なら true
     */
    public void cancel(String orderId, String requesterId, boolean admin) {
        // 注文がないとき
        if (!statusByOrder.containsKey(orderId)) {
            // 拒否する
            throw new OrderValidationException("注文が見つかりません: " + orderId);
        }
        // 【認可ポリシーの仕事】運用担当者でも本人でもないとき
        if (!admin && !ownerByOrder.get(orderId).equals(requesterId)) {
            // 権限がないので拒否する
            throw new OrderValidationException("他人の注文はキャンセルできません: " + orderId);
        }
        // 【エンティティ（集約）の仕事】キャンセルできない状態のとき
        if (!"PLACED".equals(statusByOrder.get(orderId))) {
            // 状態のルールに反するので拒否する
            throw new OrderValidationException("この状態ではキャンセルできません");
        }
        // 状態をキャンセル済みに変える
        statusByOrder.put(orderId, "CANCELLED");
    }
}
