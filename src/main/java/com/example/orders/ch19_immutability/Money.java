package com.example.orders.ch19_immutability;

/**
 * イミュータブルな金額です。技法: final クラス、private final フィールド、生成時の検証、
 * 計算結果を新しいインスタンスで返す、共有してよい定数。
 */
public final class Money {
    /** 0円。イミュータブルなので、1つを全員で共有してよい。 */
    public static final Money ZERO = new Money(0);

    // 金額（円）を持つフィールド。final なので、一度決めたら変わらない
    private final int yen;

    /**
     * 金額を作ります。外からは of で作ります。
     *
     * @param yen 金額（円）
     */
    private Money(int yen) {
        // 引数の金額を、フィールドに保存する
        this.yen = yen;
    }

    /**
     * 金額を作ります。負の金額は作れません。
     *
     * @param yen 金額（円）
     * @return 金額
     */
    public static Money of(int yen) {
        // 金額が負のとき
        if (yen < 0) {
            // ★重要★ 不正な値は、作る時点で拒否する。作れた金額は、ずっと正しいまま
            throw new IllegalArgumentException("金額は0円以上にしてください: " + yen);
        }
        // 0円なら、共有の定数を返す（新しく作らない）
        return yen == 0 ? ZERO : new Money(yen);
    }

    /**
     * 金額を足します。自分は変わらず、結果を新しい金額として返します。
     *
     * @param other 足す金額
     * @return 足した結果の、新しい金額
     */
    public Money plus(Money other) {
        // ★重要★ 自分のフィールドは書き換えず、結果を新しいインスタンスで返す
        return Money.of(yen + other.yen);
    }

    /**
     * 金額を数量倍します。自分は変わりません。
     *
     * @param quantity 掛ける数量
     * @return 掛けた結果の、新しい金額
     */
    public Money times(Quantity quantity) {
        // 金額に数量を掛けた、新しい金額を返す
        return Money.of(yen * quantity.value());
    }

    /** @return 金額（円） */
    public int yen() { return yen; }

    /**
     * 金額が同じなら等しいとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // 相手も金額で、円の値が同じなら等しい
        return o instanceof Money && ((Money) o).yen == yen;
    }

    /** @return 金額から計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 金額から計算したハッシュ値を返す（equals と整合させる）
        return Integer.hashCode(yen);
    }
}
