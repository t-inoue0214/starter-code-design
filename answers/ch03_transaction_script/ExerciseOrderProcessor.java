package com.example.orders.ch03_transaction_script.exercise;

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
 * 第3章の解答例です。1つのユースケース（注文を処理する）を、手順の名前を並べた手続きとして書いています。
 * これがトランザクションスクリプトの標準形です。外から呼べるのは {@code process} だけです。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    private final Map<String, Integer> stock;
    private final PrintStream out;

    /**
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        this.stock = stock;
        this.out = out;
    }

    /**
     * 注文を処理します。手順の名前を並べた、読みやすい手続きにします。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // 不正な入力は、ここで止める
        validate(order);
        // 金額は、小計 → 割引 → 請求額の順に求める
        int subtotal = subtotalOf(order);
        int discount = discountOf(order, subtotal);
        int total = subtotal - discount;
        // 在庫を引き当ててから決済する。決済に失敗したら、在庫は戻される
        reserveStock(order);
        charge(order, total);
        // 決済が通ってから、利用者へ知らせ、記録を残す
        notifyCustomer(order, total);
        writeAuditLog(order, total);
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /** 入力を検証します。 */
    private void validate(Order order) {
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
    }

    /** 明細の合計（割引前）を求めます。 */
    private int subtotalOf(Order order) {
        int subtotal = 0;
        for (OrderLine line : order.lines()) {
            subtotal += line.unitPrice() * line.quantity();
        }
        return subtotal;
    }

    /** 会員ランク・クーポン・12月の割引を合計します。割引は小計を超えません。 */
    private int discountOf(Order order, int subtotal) {
        int discount = rankDiscount(order.customer().rank(), subtotal)
                + couponDiscount(order.couponCode(), subtotal);
        if (order.orderedOn().getMonthValue() == 12) {
            discount += subtotal * 3 / 100;
        }
        return Math.min(discount, subtotal);
    }

    /**
     * 会員ランクに応じた割引額を求めます。ゴールドは10%、シルバーは5%、それ以外は0です。
     *
     * @param rank 会員ランク
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    private int rankDiscount(MemberRank rank, int subtotal) {
        if (rank == MemberRank.GOLD) {
            return subtotal * 10 / 100;
        }
        if (rank == MemberRank.SILVER) {
            return subtotal * 5 / 100;
        }
        return 0;
    }

    /**
     * クーポンによる割引額を求めます。
     *
     * @param coupon クーポンコード（なければ null）
     * @param subtotal 小計（円）
     * @return 割引額（円）
     */
    private int couponDiscount(String coupon, int subtotal) {
        if (coupon == null) {
            return 0;
        }
        if (coupon.equals("WELCOME500")) {
            return 500;
        }
        if (coupon.equals("SAVE10")) {
            return subtotal * 10 / 100;
        }
        throw new OrderValidationException("未知のクーポンです: " + coupon);
    }

    /** 全明細の在庫を確認してから、引き当てます。 */
    private void reserveStock(Order order) {
        for (OrderLine line : order.lines()) {
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        changeStock(order, -1);
    }

    /** 決済します。50万円を超える注文は拒否し、引き当てた在庫を戻します。 */
    private void charge(Order order, int total) {
        if (total > 500000) {
            changeStock(order, +1);
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);
    }

    /**
     * 全明細の在庫を増減します。
     *
     * @param order 注文
     * @param sign 在庫を減らすなら -1、戻すなら +1
     */
    private void changeStock(Order order, int sign) {
        for (OrderLine line : order.lines()) {
            stock.put(line.productId(), stock.get(line.productId()) + sign * line.quantity());
        }
    }

    /** メールとSMSで知らせます。 */
    private void notifyCustomer(Order order, int total) {
        if (order.customer().email() != null) {
            out.println("[MAIL] to=" + order.customer().email() + " order=" + order.id() + " total=" + total);
        }
        if (order.customer().phone() != null && total >= 10000) {
            out.println("[SMS] to=" + order.customer().phone() + " order=" + order.id());
        }
    }

    /**
     * 監査ログを出力します。
     *
     * @param order 注文
     * @param total 請求額（円）
     */
    private void writeAuditLog(Order order, int total) {
        out.println("[AUDIT] order=" + order.id() + " customer=" + order.customer().id() + " total=" + total);
    }
}
