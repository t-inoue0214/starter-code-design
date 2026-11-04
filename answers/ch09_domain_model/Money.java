package com.example.orders.ch09_domain_model.exercise;

/**
 * 金額を表す値オブジェクト（Value Object）です。不変で、金額が同じなら等しいとみなします。
 */
public final class Money {
    // 金額（円）を持つフィールド
    private final int yen;

    /**
     * 金額を作ります。負の金額は作れません。外からは of で作ります。
     *
     * @param yen 金額（円）
     */
    private Money(int yen) {
        // ★重要★ 負の金額は作れない（不正な状態を、作った時点で防ぐ）
        if (yen < 0) {
            // 負の金額は作れないので、例外を投げる
            throw new IllegalArgumentException("金額は0円以上にしてください: " + yen);
        }
        // 引数の金額（円）を、フィールドに保存する
        this.yen = yen;
    }

    /**
     * @param yen 金額（円）
     * @return Money
     */
    public static Money of(int yen) {
        // 円の数値から、金額を作って返す
        return new Money(yen);
    }

    /** @return 0円 */
    public static Money zero() {
        // 0円を返す
        return new Money(0);
    }

    /** @return 金額（円） */
    public int yen() {
        // 円の数値を返す
        return yen;
    }

    /**
     * @param other 足す金額
     * @return 合計
     */
    public Money plus(Money other) {
        // 2つの金額を足した、新しい金額を返す（元の金額は変えない）
        return new Money(yen + other.yen);
    }

    /**
     * @param other 引く金額
     * @return 差
     */
    public Money minus(Money other) {
        // 2つの金額を引いた、新しい金額を返す（負になれば例外）
        return new Money(yen - other.yen);
    }

    /**
     * @param quantity 数量
     * @return 数量倍した金額
     */
    public Money times(int quantity) {
        // 金額を数量倍にした、新しい金額を返す
        return new Money(yen * quantity);
    }

    /**
     * @param rate 割合（%）
     * @return 割合を掛けた金額（円未満は切り捨て）
     */
    public Money percent(int rate) {
        // 金額の割合（端数は切り捨て）を、新しい金額として返す
        return new Money(yen * rate / 100);
    }

    /**
     * @param other 比べる金額
     * @return この金額のほうが大きければ true
     */
    public boolean isGreaterThan(Money other) {
        // 相手より大きいかどうかを返す
        return yen > other.yen;
    }

    /**
     * @param other 比べる金額
     * @return 小さいほうの金額
     */
    public Money min(Money other) {
        // 小さいほうの金額を返す
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
        // 相手が金額で、円の数値が同じなら等しい
        return o instanceof Money && ((Money) o).yen == yen;
    }

    /**
     * 金額から計算したハッシュ値を返します。
     *
     * @return ハッシュ値
     */
    @Override
    public int hashCode() {
        // 金額のハッシュ値を返す
        return Integer.hashCode(yen);
    }

    /**
     * 金額を表す文字列を返します。
     *
     * @return 金額の文字列
     */
    @Override
    public String toString() {
        // 円付きの文字列を返す
        return yen + "円";
    }
}
