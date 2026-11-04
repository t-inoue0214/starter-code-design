package com.example.orders.ch14_decorator.before;

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
 * 注文処理のすべてに加えて、ログ出力と実行時間の計測まで本体に混ざった神クラスです。
 * 第14章では、try / catch / finally で本体を包んでいる部分に注目します。
 * 学習者は、このクラスを編集せず「読むだけ」にしてください。
 */
public class LoggingInlineOrderProcessor implements OrderProcessor {
    // 商品IDごとの在庫数を持つフィールド
    private final Map<String, Integer> stock;
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * 神クラスを生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public LoggingInlineOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // 引数の商品IDごとの在庫数を、フィールドに保存する
        this.stock = stock;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 注文を処理します。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // ログと計測の準備
        long startedAt = System.nanoTime();
        // ログに出す注文IDを取り出す（注文がなければ null）
        String orderId = order == null ? null : order.id();
        // 処理の開始をログに出力する
        out.println("[LOG] start order=" + orderId);
        // 例外が起こりうる処理を、ここから試す
        try {
            // 1. 入力検証
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

            // 2. 金額計算
            int subtotal = 0;
            // 注文の明細を1行ずつ取り出して処理する
            for (OrderLine line : order.lines()) {
                // 単価 × 数量を、小計に加える
                subtotal += line.unitPrice() * line.quantity();
            }
            // 割引額。以下のルールで加算していく
            int discount = 0;
            // 会員ランク割引: ゴールドは10%、シルバーは5%
            if (order.customer().rank() == MemberRank.GOLD) {
                // 小計の10%を、割引額に加える
                discount += subtotal * 10 / 100;
            // シルバー会員なら
            } else if (order.customer().rank() == MemberRank.SILVER) {
                // 小計の5%を、割引額に加える
                discount += subtotal * 5 / 100;
            }
            // クーポン割引: コードごとに割引の方法が違う
            String coupon = order.couponCode();
            // クーポンが指定されているときだけ
            if (coupon != null) {
                // WELCOME500 は、500円引きのクーポン
                if (coupon.equals("WELCOME500")) {
                    // 500円を、割引額に加える
                    discount += 500;
                // SAVE10 は、小計の10%引きのクーポン
                } else if (coupon.equals("SAVE10")) {
                    // 小計の10%を、割引額に加える
                    discount += subtotal * 10 / 100;
                // それ以外のとき
                } else {
                    // 登録されていないクーポンは、検証エラーとして投げる
                    throw new OrderValidationException("未知のクーポンです: " + coupon);
                }
            }
            // 12月は、全員に3%の割引を足す（季節割引）
            if (order.orderedOn().getMonthValue() == 12) {
                // 小計の3%を、季節割引として加える
                discount += subtotal * 3 / 100;
            }
            // 割引が小計を超えないようにする（請求額が負にならない）
            if (discount > subtotal) {
                // 割引額を小計までに抑える
                discount = subtotal;
            }
            // 請求額 = 小計 − 割引
            int total = subtotal - discount;

            // 3. 在庫引当（全明細の在庫を確認してから引き当てる）
            for (OrderLine line : order.lines()) {
                // 商品の現在の在庫数を調べる（登録がなければ0）
                int available = stock.getOrDefault(line.productId(), 0);
                // 在庫が注文の数量に足りないとき
                if (available < line.quantity()) {
                    // 在庫が足りないので、在庫不足の例外を投げる
                    throw new OutOfStockException("在庫が不足しています: " + line.productId());
                }
            }
            // 注文の明細を1行ずつ取り出して処理する
            for (OrderLine line : order.lines()) {
                // 商品の在庫を、注文の数量だけ減らす
                stock.put(line.productId(), stock.get(line.productId()) - line.quantity());
            }

            // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
            if (total > 500000) {
                // 注文の明細を1行ずつ取り出して処理する
                for (OrderLine line : order.lines()) {
                    // 商品の在庫を、注文の数量だけ戻す
                    stock.put(line.productId(), stock.get(line.productId()) + line.quantity());
                }
                // 決済の上限を超えたので、決済拒否の例外を投げる
                throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
            }
            // 決済の記録を出力する（実際の決済サービスの代わり）
            out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

            // 5. 通知
            if (order.customer().email() != null) {
                // メール通知を出力する（宛先、注文ID、請求額）
                out.println("[MAIL] to=" + order.customer().email()
                        + " order=" + order.id() + " total=" + total);
            }
            // SMS は、電話番号があり、1万円以上の注文だけに送る
            if (order.customer().phone() != null && total >= 10000) {
                // SMS通知を出力する（宛先、注文ID）
                out.println("[SMS] to=" + order.customer().phone()
                        + " order=" + order.id());
            }

            // 6. 監査ログ
            out.println("[AUDIT] order=" + order.id()
                    + " customer=" + order.customer().id()
                    + " total=" + total);

            // 成功のログ
            OrderResult result = new OrderResult(order.id(), subtotal, discount, total);
            // 処理の成功をログに出力する
            out.println("[LOG] end order=" + orderId + " total=" + total);
            // 処理の結果を、そのまま返す
            return result;
        // 本体の処理が失敗したとき
        } catch (RuntimeException e) {
            // 失敗のログ
            out.println("[LOG] failed order=" + orderId + " reason=" + e.getMessage());
            // 記録したあとで、例外を呼び出し側へ投げ直す
            throw e;
        // 成功しても失敗しても、最後に必ず実行する
        } finally {
            // 実行時間の計測
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            // かかった時間を出力する
            out.println("[TIME] order=" + orderId + " elapsed=" + elapsedMillis + "ms");
        }
    }
}
