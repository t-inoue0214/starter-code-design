package com.example.orders.domain;

/**
 * 注文の入力が不正なときに投げられる例外です。
 */
public class OrderValidationException extends RuntimeException {
    /**
     * 例外を生成します。
     *
     * @param message 理由
     */
    public OrderValidationException(String message) {
        // 親クラスのコンストラクタに、メッセージを渡す
        super(message);
    }
}
