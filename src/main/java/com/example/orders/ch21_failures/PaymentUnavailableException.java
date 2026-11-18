package com.example.orders.ch21_failures;

/**
 * 決済を一時的に使えないことを表す、この教材の例外です。
 * 外の部品の IOException を、業務の言葉の例外に包み直したものです。元の例外は、原因として残します。
 */
public final class PaymentUnavailableException extends RuntimeException {
    // 直列化のための番号
    private static final long serialVersionUID = 1L;

    /**
     * 例外を作ります。
     *
     * @param cause 元の例外
     */
    public PaymentUnavailableException(Throwable cause) {
        // ★重要★ 原因を捨てずに渡す。調査のときに、本当の理由をたどれるようにするため
        super("決済を利用できません", cause);
    }
}
