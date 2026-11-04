package com.example.orders.ch15_factory.exercise;

import com.example.orders.domain.OrderValidationException;

/**
 * クーポンコードから、使うべき割引ルールを作る工場です。
 * 「どのコードがどのルールか」という知識は、このクラスだけが持ちます。
 */
public class CouponRuleFactory {
    /**
     * クーポンコードに対応する割引ルールを作ります。
     *
     * @param code クーポンコード（なければ null）
     * @return 割引ルール
     * @throws OrderValidationException 未知のコードのとき
     */
    public CouponRule create(String code) {
        // クーポンなしは「割引0」のルールとして扱い、呼び出し側の null 判定をなくす
        if (code == null) {
            // ★重要★ クーポンなしは「割引0」のルールとして返す（呼び出し側の null 判定が要らない）
            return subtotal -> 0;
        }
        // WELCOME500 は、500円引きのクーポン
        if (code.equals("WELCOME500")) {
            // 500円引きのルールを作って返す
            return new FixedAmountCoupon(500);
        }
        // SAVE10 は、小計の10%引きのクーポン
        if (code.equals("SAVE10")) {
            // 10%引きのルールを作って返す
            return new PercentCoupon(10);
        }
        // 登録されていないクーポンを、検証エラーとして投げる
        throw new OrderValidationException("未知のクーポンです: " + code);
    }
}
