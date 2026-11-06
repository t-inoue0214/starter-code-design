package com.example.orders.ch17_class_kinds;

import java.util.ArrayList;
import java.util.List;

/**
 * バリデータの例です。入力の形式や範囲を検査する専用のクラスです。
 * 最初のエラーで止めず、見つかったエラーをすべて集めて返します。
 * 検査するのは、入力そのものの形式と範囲だけです。DB を見ないと分からない判断（注文IDの重複など）は、業務ロジックの役割です。
 */
public class OrderInputValidator {
    /**
     * 注文の入力を検査します。
     *
     * @param orderId    注文ID
     * @param customerId 顧客ID
     * @param quantity   数量
     * @return 見つかったエラーの一覧（問題がなければ空）
     */
    public List<ValidationError> validate(String orderId, String customerId, int quantity) {
        // ★重要★ エラーを集める入れ物を作る（例外で止めず、全部集めて返す）
        List<ValidationError> errors = new ArrayList<>();
        // 注文IDが空のとき
        if (orderId == null || orderId.isBlank()) {
            // 注文IDの誤りを、エラーコードとして集める
            errors.add(ValidationError.ORDER_ID_REQUIRED);
        }
        // 顧客IDが空のとき
        if (customerId == null || customerId.isBlank()) {
            // 顧客IDの誤りを、エラーコードとして集める
            errors.add(ValidationError.CUSTOMER_ID_REQUIRED);
        }
        // 数量が、1未満か上限を超えているとき
        if (quantity < 1 || quantity > OrderLimits.MAX_QUANTITY_PER_LINE) {
            // 数量の誤りを、エラーコードとして集める
            errors.add(ValidationError.QUANTITY_OUT_OF_RANGE);
        }
        // 集めたエラーの一覧を返す（問題がなければ空）
        return errors;
    }
}
