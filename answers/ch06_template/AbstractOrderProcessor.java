package com.example.orders.ch06_template.exercise;

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
 * 注文処理の骨格（手順）を定義する抽象クラスです。
 * 手順（検証、金額計算、在庫、決済、通知、監査）は固定し、チャネルごとに違う部分だけを子クラスに任せます。
 */
public abstract class AbstractOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    /** 子クラスが出力に使う出力先 */
    protected final PrintStream out;

    /**
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    protected AbstractOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        this.stock = stock;
        this.out = out;
    }

    /**
     * 決済を受け付ける上限額です。既定は50万円で、必要なら子クラスが上書きします（フックメソッド）。
     *
     * @return 上限額（円）
     */
    protected int paymentLimit() {
        return 500000;
    }

    /**
     * 注文成立後に、顧客へ通知します。チャネルごとに必ず決める部分です（抽象メソッド）。
     *
     * @param order  注文
     * @param result 金額の内訳
     */
    protected abstract void notifyCustomer(Order order, OrderResult result);

    /**
     * 監査ログの末尾に付ける文字列です。既定は付けません（フックメソッド）。
     *
     * @return 末尾に付ける文字列
     */
    protected String auditSuffix() {
        return "";
    }

    @Override
    public final OrderResult process(Order order) {
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
        int discount = 0;
        if (order.customer().rank() == MemberRank.GOLD) {
            discount += subtotal * 10 / 100;
        } else if (order.customer().rank() == MemberRank.SILVER) {
            discount += subtotal * 5 / 100;
        }
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
        if (order.orderedOn().getMonthValue() == 12) {
            discount += subtotal * 3 / 100;
        }
        if (discount > subtotal) {
            discount = subtotal;
        }
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

        // 4. 決済（上限額を超える注文は拒否。拒否されたら在庫を戻す）
        if (total > paymentLimit()) {
            for (OrderLine line : order.lines()) {
                stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
            }
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知（チャネルごとの部分）
        OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
        notifyCustomer(order, result);

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total
                + auditSuffix());

        return result;
    }
}
