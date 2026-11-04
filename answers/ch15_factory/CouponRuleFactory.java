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
            return subtotal -> 0;
        }
        if (code.equals("WELCOME500")) {
            return new FixedAmountCoupon(500);
        }
        if (code.equals("SAVE10")) {
            return new PercentCoupon(10);
        }
        throw new OrderValidationException("未知のクーポンです: " + code);
    }
}
