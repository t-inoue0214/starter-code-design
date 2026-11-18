package com.example.orders.ch21_failures;

import java.io.IOException;

/**
 * 決済会社が配っているライブラリの代わりです。通信に失敗すると、検査例外の IOException を投げます。
 * 外の部品の作りは、こちらで選べません。
 */
public interface PaymentClient {

    /**
     * 決済の承認を求めます。
     *
     * @param customerId 顧客ID
     * @param amount     金額（円）
     * @return 承認されれば true、断られれば false
     * @throws IOException 決済会社と通信できなかったとき
     */
    boolean authorize(String customerId, int amount) throws IOException;
}
