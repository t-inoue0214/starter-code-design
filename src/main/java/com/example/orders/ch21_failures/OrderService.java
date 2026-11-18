package com.example.orders.ch21_failures;

import java.util.List;
import java.util.Optional;

/**
 * 注文を受け付けるサービスです。失敗を種類ごとに分けて扱います。
 * 入力の誤りと業務の失敗は戻り値で返し、決済の一時的な停止は例外のまま呼び出し側へ伝えます。
 */
public final class OrderService {
    // 1回の注文の上限金額（円）
    static final int MAX_TOTAL = 100_000;
    // 入力の形を検査するバリデータを持つフィールド
    private final OrderRequestValidator validator = new OrderRequestValidator();
    // 商品の台帳を持つフィールド
    private final StockBook stockBook;
    // 決済のゲートウェイを持つフィールド
    private final PaymentGateway payment;
    // 発行した注文の数を持つフィールド
    private int issued;

    /**
     * サービスを作ります。
     *
     * @param stockBook 商品の台帳
     * @param payment   決済のゲートウェイ
     */
    public OrderService(StockBook stockBook, PaymentGateway payment) {
        // 引数の台帳を、フィールドに保存する
        this.stockBook = stockBook;
        // 引数のゲートウェイを、フィールドに保存する
        this.payment = payment;
    }

    /**
     * 注文を受け付けます。
     *
     * @param request 注文の入力
     * @return 注文の結果
     */
    public PlaceOrderResult place(OrderRequest request) {
        // ★重要★ 1. 入口で、形の誤りをすべて集める
        List<FieldError> errors = validator.validate(request);
        // 誤りがあるとき
        if (!errors.isEmpty()) {
            // 入力の誤りとして返す
            return PlaceOrderResult.invalid(errors);
        }
        // 検証済みの数字から、数量を作る
        Quantity quantity = Quantity.of(Integer.parseInt(request.quantityText().trim()));
        // ★重要★ 2. データを見ないと分からない誤りは、サービスで確かめる
        Optional<Integer> unitPrice = stockBook.unitPriceOf(request.productId());
        // 商品が見つからないとき
        if (unitPrice.isEmpty()) {
            // 業務の失敗として返す
            return PlaceOrderResult.rejected(OrderFailure.UNKNOWN_PRODUCT);
        }
        // 在庫が足りないとき
        if (stockBook.stockOf(request.productId()) < quantity.value()) {
            // 業務の失敗として返す
            return PlaceOrderResult.rejected(OrderFailure.OUT_OF_STOCK);
        }
        // 合計を計算する
        int total = unitPrice.get() * quantity.value();
        // 上限金額を超えるとき
        if (total > MAX_TOTAL) {
            // 業務の失敗として返す
            return PlaceOrderResult.rejected(OrderFailure.OVER_LIMIT);
        }
        // ★重要★ 3. 決済する。一時的な停止の例外は、ここでは捕まえず、入口まで伝える
        if (!payment.charge(request.customerId(), total)) {
            // 断られたことは業務の失敗として返す（在庫は減らしていない）
            return PlaceOrderResult.rejected(OrderFailure.PAYMENT_DECLINED);
        }
        // 決済が通ったあとで、在庫を減らす
        stockBook.take(request.productId(), quantity.value());
        // 発行した注文の数を増やす
        issued++;
        // 成功の結果を返す
        return PlaceOrderResult.success("O-" + issued, total);
    }
}
