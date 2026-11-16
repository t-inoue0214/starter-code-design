package com.example.orders.ch19_immutability;

/**
 * イミュータブルな数量です。技法: 生成時の検証と、変更したいときは新しいインスタンスを返すこと。
 */
public final class Quantity {
    // 数量の下限
    private static final int MIN = 1;
    // 数量の上限
    private static final int MAX = 99;

    // 数量を持つフィールド
    private final int value;

    /**
     * 数量を作ります。外からは of で作ります。
     *
     * @param value 数量
     */
    private Quantity(int value) {
        // 引数の数量を、フィールドに保存する
        this.value = value;
    }

    /**
     * 数量を作ります。範囲外の数量は作れません。
     *
     * @param value 数量（1〜99）
     * @return 数量
     */
    public static Quantity of(int value) {
        // 範囲外のとき
        if (value < MIN || value > MAX) {
            // 不正な値として拒否する
            throw new IllegalArgumentException("数量は1〜99にしてください: " + value);
        }
        // 新しい数量を返す
        return new Quantity(value);
    }

    /**
     * 数量を足した、新しい数量を返します。
     *
     * @param extra 足す数
     * @return 足した結果の、新しい数量
     */
    public Quantity plus(int extra) {
        // ★重要★ 新しい値も of を通すので、足した結果が範囲外なら、ここで拒否される
        return Quantity.of(value + extra);
    }

    /** @return 数量 */
    public int value() { return value; }

    /**
     * 数量が同じなら等しいとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // 相手も数量で、値が同じなら等しい
        return o instanceof Quantity && ((Quantity) o).value == value;
    }

    /** @return 数量から計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 数量から計算したハッシュ値を返す（equals と整合させる）
        return Integer.hashCode(value);
    }
}
