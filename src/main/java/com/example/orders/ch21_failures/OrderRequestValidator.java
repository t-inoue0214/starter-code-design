package com.example.orders.ch21_failures;

import java.util.ArrayList;
import java.util.List;

/**
 * 入口で、入力の形を検査するバリデータです。必須、数字かどうか、範囲だけを見ます。
 * 在庫や商品の有無のように、データを見ないと分からないことは検査しません。
 */
public final class OrderRequestValidator {
    // 数量の下限
    static final int MIN_QUANTITY = 1;
    // 数量の上限
    static final int MAX_QUANTITY = 99;

    /**
     * 入力を検査し、見つかった誤りをすべて返します。
     *
     * @param request 検査する入力
     * @return 誤りの一覧（誤りがなければ空）
     */
    public List<FieldError> validate(OrderRequest request) {
        // 誤りを集める一覧を用意する
        List<FieldError> errors = new ArrayList<>();
        // 顧客IDが空のとき
        if (isBlank(request.customerId())) {
            // 誤りを集める（ここで止めない）
            errors.add(new FieldError("customerId", "顧客IDを入力してください"));
        }
        // 商品IDが空のとき
        if (isBlank(request.productId())) {
            // 誤りを集める
            errors.add(new FieldError("productId", "商品IDを入力してください"));
        }
        // 数量を確かめて、誤りがあれば集める
        checkQuantity(request.quantityText(), errors);
        // ★重要★ 最初の誤りで止めず、すべて集めて返す。利用者が一度に直せるようにするため
        return List.copyOf(errors);
    }

    /**
     * 数量の文字列が、範囲内の整数かを確かめます。
     *
     * @param text   数量の文字列
     * @param errors 誤りを足す一覧
     */
    private static void checkQuantity(String text, List<FieldError> errors) {
        // 数量が空のとき
        if (isBlank(text)) {
            // 誤りを足す
            errors.add(new FieldError("quantity", "数量を入力してください"));
            // これ以上は確かめない
            return;
        }
        // 例外が起こりうる変換を、ここから試す
        try {
            // 文字列を整数に変える
            int quantity = Integer.parseInt(text.trim());
            // 範囲の外のとき
            if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
                // 誤りを足す
                errors.add(new FieldError("quantity", "数量は" + MIN_QUANTITY + "〜" + MAX_QUANTITY + "で入力してください"));
            }
        // 整数に変えられなかったとき
        } catch (NumberFormatException e) {
            // ★重要★ 入力の誤りは、例外のまま外へ出さず、利用者向けの誤りに変える
            errors.add(new FieldError("quantity", "数量は数字で入力してください"));
        }
    }

    /**
     * 文字列が null か、空白だけかを確かめます。
     *
     * @param text 確かめる文字列
     * @return 空なら true
     */
    private static boolean isBlank(String text) {
        // null か、空白だけなら空とみなす
        return text == null || text.isBlank();
    }
}
