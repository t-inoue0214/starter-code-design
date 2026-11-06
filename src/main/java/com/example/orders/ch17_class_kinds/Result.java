package com.example.orders.ch17_class_kinds;

/**
 * Result（結果）の例です。「成功した値」か「失敗の理由」のどちらかを、戻り値として返します。
 * 例外を使わず、呼び出し側に成否の確認を促したいときに使います。
 *
 * @param <T> 成功したときの値の型
 */
public final class Result<T> {
    // 値を持つフィールド
    private final T value;
    // 失敗の理由を持つフィールド
    private final String error;

    /**
     * 成功の値か、失敗の理由のどちらかから作ります。外からは ok と failure を使います。
     *
     * @param value 成功した値（失敗なら null）
     * @param error 失敗の理由（成功なら null）
     */
    private Result(T value, String error) {
        // 引数の値を、フィールドに保存する
        this.value = value;
        // 引数の失敗の理由を、フィールドに保存する
        this.error = error;
    }

    /**
     * 成功の結果を作ります。
     *
     * @param value 成功した値
     * @param <T>   値の型
     * @return 成功の結果
     */
    public static <T> Result<T> ok(T value) {
        // ★重要★ 成功の結果を返す（失敗の理由は null）
        return new Result<>(value, null);
    }

    /**
     * 失敗の結果を作ります。
     *
     * @param error 失敗の理由
     * @param <T>   値の型
     * @return 失敗の結果
     */
    public static <T> Result<T> failure(String error) {
        // ★重要★ 失敗の結果を返す（成功した値は null）
        return new Result<>(null, error);
    }

    /** @return 成功なら true */
    public boolean isOk() {
        // 失敗の理由がなければ、成功
        return error == null;
    }

    /** @return 成功した値。失敗の結果から取り出そうとすると例外 */
    public T value() {
        // 失敗の結果から、値を取り出そうとしたとき
        if (!isOk()) {
            // 取り出す前に isOk で確かめるよう、呼び出し側に促す
            throw new IllegalStateException("失敗の結果から値は取り出せません: " + error);
        }
        // 成功した値を返す
        return value;
    }

    /** @return 失敗の理由。成功なら null */
    public String error() { return error; }
}
