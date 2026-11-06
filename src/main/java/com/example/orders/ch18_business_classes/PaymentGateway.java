package com.example.orders.ch18_business_classes;

/**
 * ゲートウェイの窓口です。外部の決済サービスとのやり取りを、業務ロジックから隠します。
 * 業務ロジックは、この窓口だけを知り、通信の細部（URL、形式、認証）は知りません。
 */
public interface PaymentGateway {

    /**
     * 決済を請求します。
     *
     * @param customerId 請求する顧客のID
     * @param amount     請求額（円）
     * @throws com.example.orders.domain.PaymentDeclinedException 外部サービスが拒否したとき
     */
    void charge(String customerId, int amount);
}
