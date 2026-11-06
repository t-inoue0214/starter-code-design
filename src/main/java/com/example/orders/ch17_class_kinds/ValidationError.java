package com.example.orders.ch17_class_kinds;

/**
 * 入力の誤りを表す Enum です。エラーコードとメッセージを、セットで持ちます。
 * バリデータは、この Enum の一覧を返します。コードは、ログの検索や API のエラー応答に使えます。
 */
public enum ValidationError {
    /** 注文IDが空。 */
    ORDER_ID_REQUIRED("E001", "注文IDは必須です"),
    /** 顧客IDが空。 */
    CUSTOMER_ID_REQUIRED("E002", "顧客IDは必須です"),
    /** 数量が範囲外。 */
    QUANTITY_OUT_OF_RANGE("E003", "数量は1以上" + OrderLimits.MAX_QUANTITY_PER_LINE + "以下にしてください");

    // クーポンコードを持つフィールド
    private final String code;
    // messageを持つフィールド
    private final String message;

    /**
     * エラーコードと、利用者に見せるメッセージを設定します。
     *
     * @param code    エラーコード（ログや API で使う、変わらない識別子）
     * @param message 利用者に見せるメッセージ
     */
    ValidationError(String code, String message) {
        // 引数のクーポンコードを、フィールドに保存する
        this.code = code;
        // 引数のmessageを、フィールドに保存する
        this.message = message;
    }

    /** @return エラーコード */
    public String code() { return code; }

    /** @return 利用者に見せるメッセージ */
    public String message() { return message; }
}
