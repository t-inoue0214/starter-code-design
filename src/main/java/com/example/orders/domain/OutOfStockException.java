package com.example.orders.domain;

/**
 * 在庫が不足しているときに投げられる例外です。
 */
public class OutOfStockException extends RuntimeException {
    /**
     * 例外を生成します。
     *
     * @param message 理由
     */
    public OutOfStockException(String message) {
        super(message);
    }
}
