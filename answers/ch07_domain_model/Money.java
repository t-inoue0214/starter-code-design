package com.example.orders.ch07_domain_model.exercise;

/**
 * 金額を表す値オブジェクト（Value Object）です。不変で、金額が同じなら等しいとみなします。
 */
public final class Money {
    private final int yen;

    private Money(int yen) {
        // 負の金額は作れない（不正な状態を、作った時点で防ぐ）
        if (yen < 0) {
            throw new IllegalArgumentException("金額は0円以上にしてください: " + yen);
        }
        this.yen = yen;
    }

    /**
     * @param yen 金額（円）
     * @return Money
     */
    public static Money of(int yen) {
        return new Money(yen);
    }

    /** @return 0円 */
    public static Money zero() {
        return new Money(0);
    }

    /** @return 金額（円） */
    public int yen() {
        return yen;
    }

    /**
     * @param other 足す金額
     * @return 合計
     */
    public Money plus(Money other) {
        return new Money(yen + other.yen);
    }

    /**
     * @param other 引く金額
     * @return 差
     */
    public Money minus(Money other) {
        return new Money(yen - other.yen);
    }

    /**
     * @param quantity 数量
     * @return 数量倍した金額
     */
    public Money times(int quantity) {
        return new Money(yen * quantity);
    }

    /**
     * @param rate 割合（%）
     * @return 割合を掛けた金額（円未満は切り捨て）
     */
    public Money percent(int rate) {
        return new Money(yen * rate / 100);
    }

    /**
     * @param other 比べる金額
     * @return この金額のほうが大きければ true
     */
    public boolean isGreaterThan(Money other) {
        return yen > other.yen;
    }

    /**
     * @param other 比べる金額
     * @return 小さいほうの金額
     */
    public Money min(Money other) {
        return yen <= other.yen ? this : other;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money && ((Money) o).yen == yen;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(yen);
    }

    @Override
    public String toString() {
        return yen + "円";
    }
}
