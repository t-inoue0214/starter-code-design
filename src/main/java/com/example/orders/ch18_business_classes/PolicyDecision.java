package com.example.orders.ch18_business_classes;

/**
 * ポリシー（判定だけをするクラス）が返す結果です。可否と、否のときの理由を持ちます。
 * 例外ではなく値で返すので、呼び出し側が「どう扱うか」を決められます。
 */
public final class PolicyDecision {
    // 許可されたかどうかを持つフィールド
    private final boolean allowed;
    // 許可されなかった理由を持つフィールド（許可のときは空文字）
    private final String reason;

    /**
     * 結果を作ります。外からは allow か deny で作ります。
     *
     * @param allowed 許可されたか
     * @param reason  許可されなかった理由
     */
    private PolicyDecision(boolean allowed, String reason) {
        // 引数の可否を、フィールドに保存する
        this.allowed = allowed;
        // 引数の理由を、フィールドに保存する
        this.reason = reason;
    }

    /**
     * 許可の結果を作ります。
     *
     * @return 許可の結果
     */
    public static PolicyDecision allow() {
        // 理由が空の、許可の結果を返す
        return new PolicyDecision(true, "");
    }

    /**
     * 不許可の結果を作ります。
     *
     * @param reason 許可されなかった理由
     * @return 不許可の結果
     */
    public static PolicyDecision deny(String reason) {
        // 理由付きの、不許可の結果を返す
        return new PolicyDecision(false, reason);
    }

    /** @return 許可されたなら true */
    public boolean allowed() { return allowed; }

    /** @return 許可されなかった理由（許可のときは空文字） */
    public String reason() { return reason; }
}
