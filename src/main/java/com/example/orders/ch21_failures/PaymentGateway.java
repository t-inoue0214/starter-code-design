package com.example.orders.ch21_failures;

import java.io.IOException;

/**
 * 決済会社のライブラリを包むゲートウェイです。外の部品の失敗を、この教材の言葉に翻訳します。
 */
public final class PaymentGateway {
    // 決済会社のライブラリを持つフィールド
    private final PaymentClient client;

    /**
     * ゲートウェイを作ります。
     *
     * @param client 決済会社のライブラリ
     */
    public PaymentGateway(PaymentClient client) {
        // 引数のライブラリを、フィールドに保存する
        this.client = client;
    }

    /**
     * 決済します。
     *
     * @param customerId 顧客ID
     * @param amount     金額（円）
     * @return 承認されれば true、断られれば false
     */
    public boolean charge(String customerId, int amount) {
        // 例外が起こりうる通信を、ここから試す
        try {
            // 決済会社に承認を求めて、答えを返す
            return client.authorize(customerId, amount);
        // 通信に失敗したとき
        } catch (IOException e) {
            // ★重要★ 外の部品の例外を、境界で翻訳する。内側のコードは IOException を知らなくてよい
            throw new PaymentUnavailableException(e);
        }
    }
}
