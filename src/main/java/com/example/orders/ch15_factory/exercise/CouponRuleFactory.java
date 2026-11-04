package com.example.orders.ch15_factory.exercise;

/**
 * クーポンコードから、使うべき割引ルールを作る工場です。
 * テストがこの形を前提にしているため、create の形は変えないでください。
 */
public class CouponRuleFactory {
    /**
     * クーポンコードに対応する割引ルールを作ります。
     * TODO: コードに応じて、適切な CouponRule を返すように書いてください。
     *       クーポンなし（null）は「割引0」のルール、未知のコードは OrderValidationException です。
     *
     * @param code クーポンコード（なければ null）
     * @return 割引ルール
     */
    public CouponRule create(String code) {
        throw new UnsupportedOperationException("TODO: 第15章の課題です");
    }
}
