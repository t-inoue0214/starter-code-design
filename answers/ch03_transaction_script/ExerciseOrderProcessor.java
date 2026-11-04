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
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * @param stock 商品IDごとの在庫数（引当のたびに書き換わります）
     * @param out   通知・決済・監査ログの出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
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
        // ★重要★ 割引の計算は、名前を付けた別のメソッドに切り出した
        int discount = discountOf(order, subtotal);
        // 請求額を、小計から割引額を引いて求める
        int total = subtotal - discount;
        // 在庫を引き当ててから決済する。決済に失敗したら、在庫は戻される
        reserveStock(order);
        // ★重要★ 決済の手順は、名前を付けた別のメソッドに切り出した
        charge(order, total);
        // 決済が通ってから、利用者へ知らせ、記録を残す
        notifyCustomer(order, total);
        // 監査ログを残す
        writeAuditLog(order, total);
        // 注文ID・小計・割引額・請求額を、結果として返す
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /** 入力を検証します。 */
    private void validate(Order order) {
        // 注文か注文者がなければ、以降の処理ができない
        if (order == null || order.customer() == null) {
            // 注文か注文者がないことを、検証エラーとして投げる
            throw new OrderValidationException("注文または注文者がありません");
        }
        // 明細がない注文は、処理できない
        if (order.lines() == null || order.lines().isEmpty()) {
            // 明細がないことを、検証エラーとして投げる
            throw new OrderValidationException("明細がありません");
        }
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 数量が0以下の明細は、不正として扱う
            if (line.quantity() <= 0) {
                // 数量が不正であることを、検証エラーとして投げる
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            // 単価が負の明細は、不正として扱う
            if (line.unitPrice() < 0) {
                // 単価が負であることを、検証エラーとして投げる
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }
    }

    /** 明細の合計（割引前）を求めます。 */
    private int subtotalOf(Order order) {
        // 小計を0から数え始める
        int subtotal = 0;
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 単価 × 数量を、小計に加える
            subtotal += line.unitPrice() * line.quantity();
        }
        // 求めた小計を返す
        return subtotal;
    }

    /** 会員ランク・クーポン・12月の割引を合計します。割引は小計を超えません。 */
    private int discountOf(Order order, int subtotal) {
        // 会員ランクの割引とクーポンの割引を合計する
        int discount = rankDiscount(order.customer().rank(), subtotal)
                + couponDiscount(order.couponCode(), subtotal);
        // 注文日が12月なら
        if (order.orderedOn().getMonthValue() == 12) {
            // 小計の3%を、季節割引として加える
            discount += subtotal * 3 / 100;
        }
        // 割引額を小計までに抑えて返す
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
        // ゴールド会員なら
        if (rank == MemberRank.GOLD) {
            // 小計の10%を返す
            return subtotal * 10 / 100;
        }
        // シルバー会員なら
        if (rank == MemberRank.SILVER) {
            // 小計の5%を返す
            return subtotal * 5 / 100;
        }
        // 割引なしとして、0 を返す
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
        // クーポンの指定がないとき
        if (coupon == null) {
            // 割引なしとして、0 を返す
            return 0;
        }
        // WELCOME500 は、500円引きのクーポン
        if (coupon.equals("WELCOME500")) {
            // WELCOME500 は、定額の500円を返す
            return 500;
        }
        // SAVE10 は、小計の10%引きのクーポン
        if (coupon.equals("SAVE10")) {
            // 小計の10%を返す
            return subtotal * 10 / 100;
        }
        // 登録されていないクーポンは、検証エラーとして投げる
        throw new OrderValidationException("未知のクーポンです: " + coupon);
    }

    /** 全明細の在庫を確認してから、引き当てます。 */
    private void reserveStock(Order order) {
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 在庫が、注文の数量に足りないとき
            if (stock.getOrDefault(line.productId(), 0) < line.quantity()) {
                // 在庫が足りないので、在庫不足の例外を投げる
                throw new OutOfStockException("在庫が不足しています: " + line.productId());
            }
        }
        // 在庫を引き当てる（符号が - なので減る）
        changeStock(order, -1);
    }

    /** 決済します。50万円を超える注文は拒否し、引き当てた在庫を戻します。 */
    private void charge(Order order, int total) {
        // 請求額が決済の上限（50万円）を超えたとき
        if (total > 500000) {
            // 引き当てた在庫を戻す（符号が + なので増える）
            changeStock(order, +1);
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);
    }

    /**
     * 全明細の在庫を増減します。
     *
     * @param order 注文
     * @param sign 在庫を減らすなら -1、戻すなら +1
     */
    private void changeStock(Order order, int sign) {
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 在庫を、数量 × 向き（減らすなら -1、戻すなら +1）だけ増減する
            stock.put(line.productId(), stock.get(line.productId()) + sign * line.quantity());
        }
    }

    /** メールとSMSで知らせます。 */
    private void notifyCustomer(Order order, int total) {
        // メールアドレスがあるときだけ
        if (order.customer().email() != null) {
            // メール通知を出力する（宛先、注文ID、請求額）
            out.println("[MAIL] to=" + order.customer().email() + " order=" + order.id() + " total=" + total);
        }
        // 電話番号があり、請求額が1万円以上のときだけ
        if (order.customer().phone() != null && total >= 10000) {
            // SMS通知を出力する（宛先、注文ID）
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
        // 監査ログを出力する（注文ID、顧客ID、請求額）
        out.println("[AUDIT] order=" + order.id() + " customer=" + order.customer().id() + " total=" + total);
    }
}
