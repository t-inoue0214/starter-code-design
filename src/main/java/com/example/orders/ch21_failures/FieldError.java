package com.example.orders.ch21_failures;

/**
 * 入力の1項目についての誤りです。どの項目が、なぜ誤りかを持ちます。
 */
public final class FieldError {
    // 項目の名前を持つフィールド
    private final String field;
    // 誤りの説明を持つフィールド
    private final String message;

    /**
     * 誤りを作ります。
     *
     * @param field   項目の名前
     * @param message 誤りの説明
     */
    public FieldError(String field, String message) {
        // 引数の項目の名前を、フィールドに保存する
        this.field = field;
        // 引数の説明を、フィールドに保存する
        this.message = message;
    }

    /** @return 項目の名前 */
    public String field() { return field; }

    /** @return 誤りの説明 */
    public String message() { return message; }
}
