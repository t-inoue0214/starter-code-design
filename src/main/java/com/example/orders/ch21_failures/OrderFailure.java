package com.example.orders.ch21_failures;

/**
 * 業務として起こりうる失敗の種類です。どれも、利用者が理由を知れば、次の行動を選べます。
 * 利用者向けの文章は持ちません。文章に変えるのは、入口の OrderController の仕事です。
 */
public enum OrderFailure {
    /** 商品が見つからない。 */
    UNKNOWN_PRODUCT,
    /** 在庫が足りない。 */
    OUT_OF_STOCK,
    /** 1回の注文の上限金額を超えた。 */
    OVER_LIMIT,
    /** 決済会社がカードを断った。 */
    PAYMENT_DECLINED
}
