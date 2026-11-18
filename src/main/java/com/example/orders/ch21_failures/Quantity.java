package com.example.orders.ch21_failures;

/**
 * 注文の数量です。作った時点で、範囲内であることが保証されます。
 * 入口のバリデータを通ったあとに作るので、ここで拒否されるのは、プログラムの誤りのときだけです。
 */
public final class Quantity {
    // 数量を持つフィールド
    private final int value;

    /**
     * 数量を作ります。外からは of を使います。
     *
     * @param value 数量
     */
    private Quantity(int value) {
        // 引数の数量を、フィールドに保存する
        this.value = value;
    }

    /**
     * 数量を作ります。範囲の外なら拒否します。
     *
     * @param value 数量
     * @return 数量
     */
    public static Quantity of(int value) {
        // 範囲の外のとき
        if (value < OrderRequestValidator.MIN_QUANTITY || value > OrderRequestValidator.MAX_QUANTITY) {
            // ★重要★ ここに来るのは、検証を通さずに呼んだプログラムの誤り。だから例外で止める
            throw new IllegalArgumentException("数量が範囲の外です: " + value);
        }
        // 数量を作って返す
        return new Quantity(value);
    }

    /** @return 数量 */
    public int value() { return value; }
}
