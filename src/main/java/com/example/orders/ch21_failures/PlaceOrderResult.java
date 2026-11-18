package com.example.orders.ch21_failures;

import java.util.List;

/**
 * 注文の結果です。「成功」「業務の失敗」「入力の誤り」の3つのどれかを表します。
 * 起こりうる失敗を戻り値で返すので、呼び出し側は、成否を確かめずに先へ進めません。
 */
public final class PlaceOrderResult {
    // 注文IDを持つフィールド（成功のときだけ）
    private final String orderId;
    // 合計金額を持つフィールド（成功のときだけ）
    private final int total;
    // 業務の失敗を持つフィールド（業務の失敗のときだけ）
    private final OrderFailure failure;
    // 入力の誤りを持つフィールド（入力の誤りのときだけ。それ以外は空）
    private final List<FieldError> errors;

    /**
     * 結果を作ります。外からは success、rejected、invalid を使います。
     *
     * @param orderId 注文ID
     * @param total   合計金額
     * @param failure 業務の失敗
     * @param errors  入力の誤り
     */
    private PlaceOrderResult(String orderId, int total, OrderFailure failure, List<FieldError> errors) {
        // 引数の注文IDを、フィールドに保存する
        this.orderId = orderId;
        // 引数の合計を、フィールドに保存する
        this.total = total;
        // 引数の業務の失敗を、フィールドに保存する
        this.failure = failure;
        // 引数の誤りの一覧を、変更できないコピーにして保存する
        this.errors = List.copyOf(errors);
    }

    /**
     * 成功の結果を作ります。
     *
     * @param orderId 注文ID
     * @param total   合計金額
     * @return 成功の結果
     */
    public static PlaceOrderResult success(String orderId, int total) {
        // 注文IDと合計だけを持つ結果を返す
        return new PlaceOrderResult(orderId, total, null, List.of());
    }

    /**
     * 業務の失敗の結果を作ります。
     *
     * @param failure 失敗の種類
     * @return 業務の失敗の結果
     */
    public static PlaceOrderResult rejected(OrderFailure failure) {
        // 失敗の種類だけを持つ結果を返す
        return new PlaceOrderResult(null, 0, failure, List.of());
    }

    /**
     * 入力の誤りの結果を作ります。
     *
     * @param errors 誤りの一覧
     * @return 入力の誤りの結果
     */
    public static PlaceOrderResult invalid(List<FieldError> errors) {
        // 誤りの一覧だけを持つ結果を返す
        return new PlaceOrderResult(null, 0, null, errors);
    }

    /** @return 成功なら true */
    public boolean isSuccess() {
        // 業務の失敗も入力の誤りもなければ、成功
        return failure == null && errors.isEmpty();
    }

    /** @return 入力の誤りなら true */
    public boolean isInvalid() {
        // 誤りの一覧が空でなければ、入力の誤り
        return !errors.isEmpty();
    }

    /** @return 注文ID（成功のときだけ） */
    public String orderId() { return orderId; }

    /** @return 合計金額（成功のときだけ） */
    public int total() { return total; }

    /** @return 業務の失敗（業務の失敗のときだけ。それ以外は null） */
    public OrderFailure failure() { return failure; }

    /** @return 入力の誤りの一覧（それ以外は空） */
    public List<FieldError> errors() { return errors; }
}
