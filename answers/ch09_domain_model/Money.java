package com.example.orders.ch09_domain_model.exercise;

/**
 * 金額を表す値オブジェクト（Value Object）です。不変で、金額が同じなら等しいとみなします。
 */
public final class Money {
    private final int yen;

    /**
     * 金額を作ります。負の金額は作れません。外からは of で作ります。
     *
     * @param yen 金額（円）
     */
    private Money(int yen) {
        // ★重要★ 負の金額は作れない（不正な状態を、作った時点で防ぐ）
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

    /**
     * 金額が同じなら、等しいとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Money && ((Money) o).yen == yen;
    }

    /**
     * 金額から計算したハッシュ値を返します。
     *
     * @return ハッシュ値
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(yen);
    }

    /**
     * 金額を表す文字列を返します。
     *
     * @return 金額の文字列
     */
    @Override
    public String toString() {
        return yen + "円";
    }
}
