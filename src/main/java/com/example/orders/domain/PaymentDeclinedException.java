package com.example.orders.domain;

/**
 * 決済が拒否されたときに投げられる例外です。
 */
public class PaymentDeclinedException extends RuntimeException {
    /**
     * 例外を生成します。
     *
     * @param message 理由
     */
    public PaymentDeclinedException(String message) {
        // 親クラスのコンストラクタに、メッセージを渡す
        super(message);
    }
}
