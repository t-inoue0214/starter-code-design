package com.example.orders.ch22_testability;

import java.time.LocalDate;

/**
 * 発行したクーポンです。作ったら変わらない値です。
 */
public final class Coupon {
    // クーポンのコードを持つフィールド
    private final String code;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 割引額（円）を持つフィールド
    private final int discount;
    // 有効期限を持つフィールド
    private final LocalDate expiresOn;

    /**
     * クーポンを作ります。
     *
     * @param code       クーポンのコード
     * @param customerId 顧客ID
     * @param discount   割引額（円）
     * @param expiresOn  有効期限
     */
    public Coupon(String code, String customerId, int discount, LocalDate expiresOn) {
        // 引数のコードを、フィールドに保存する
        this.code = code;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の割引額を、フィールドに保存する
        this.discount = discount;
        // 引数の有効期限を、フィールドに保存する
        this.expiresOn = expiresOn;
    }

    /** @return クーポンのコード */
    public String code() { return code; }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 割引額（円） */
    public int discount() { return discount; }

    /** @return 有効期限 */
    public LocalDate expiresOn() { return expiresOn; }
}
