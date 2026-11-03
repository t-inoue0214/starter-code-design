package com.example.orders.domain;

/**
 * 注文処理の公開窓口です。
 * 全章を通じて、分解前（神クラス）も分解後も、この窓口の振る舞いは変わりません。
 */
public interface OrderProcessor {
    /**
     * 注文を処理します（検証・金額計算・在庫引当・決済・通知・監査ログ）。
     *
     * @param order 注文
     * @return 金額の内訳
     * @throws OrderValidationException 入力が不正なとき
     * @throws OutOfStockException      在庫が不足しているとき
     * @throws PaymentDeclinedException 決済が拒否されたとき
     */
    OrderResult process(Order order);
}
