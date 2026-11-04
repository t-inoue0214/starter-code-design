package com.example.orders.ch01_yardstick.before;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.PrintStream;
import java.util.Map;

/**
 * 注文処理のすべてを1クラスに詰め込んだ実装（神クラス）です。
 * 動作は正しく、テストも通ります。問題は動作ではなく構造にあります。
 * 学習者は、このクラスを編集せず「読むだけ」にしてください。
 */
public class LegacyOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    private final PrintStream out;

    /**
     * 神クラスを生成します。
     *
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public LegacyOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        this.stock = stock;
        this.out = out;
    }

    /**
     * 注文を処理します。検証、金額計算、在庫引当、決済、通知、監査ログを順に行います。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // 1. 入力検証
        if (order == null || order.customer() == null) {
            throw new OrderValidationException("注文または注文者がありません");
        }
        if (order.lines() == null || order.lines().isEmpty()) {
            throw new OrderValidationException("明細がありません");
        }
        for (OrderLine line : order.lines()) {
            if (line.quantity() <= 0) {
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            if (line.unitPrice() < 0) {
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }

        // 2. 金額計算
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        // 割引額。以下のルールで加算していく
        int discount = 0;
        // 会員ランク割引: ゴールドは10%、シルバーは5%
        if (order.customer().rank() == MemberRank.GOLD) {
            discount += subtotal * 10 / 100;
        } else if (order.customer().rank() == MemberRank.SILVER) {
            discount += subtotal * 5 / 100;
        }
        // クーポン割引: コードごとに割引の方法が違う
        String coupon = order.couponCode();
        if (coupon != null) {
            if (coupon.equals("WELCOME500")) {
                discount += 500;
            } else if (coupon.equals("SAVE10")) {
                discount += subtotal * 10 / 100;
            } else {
                throw new OrderValidationException("未知のクーポンです: " + coupon);
            }
        }
        // 12月は、全員に3%の割引を足す（季節割引）
        if (order.orderedOn().getMonthValue() == 12) {
            discount += subtotal * 3 / 100;
        }
        // 割引が小計を超えないようにする（請求額が負にならない）
        if (discount > subtotal) {
            discount = subtotal;
        }
        // 請求額 = 小計 − 割引
        int total = subtotal - discount;

        // 3. 在庫引当（全明細の在庫を確認してから引き当てる）
        for (OrderLine line : order.lines()) {
            int available = stock.getOrDefault(line.productId(), 0);
            if (available < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
        }

        // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
        if (total > 500000) {
            for (OrderLine line : order.lines()) {
                stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
            }
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知
        if (order.customer().email() != null) {
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + total);
        }
        // SMS は、電話番号があり、1万円以上の注文だけに送る
        if (order.customer().phone() != null && total >= 10000) {
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total);

        // 金額の内訳を呼び出し元へ返す
        return new OrderResult(order.id(), subtotal, discount, total);
    }
}
