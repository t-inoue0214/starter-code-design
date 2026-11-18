package com.example.orders.ch21_failures;

import java.io.IOException;

/**
 * テストと動作確認のための、決済会社の偽物です。承認、拒否、通信の失敗を選べます。
 */
public final class FakePaymentClient implements PaymentClient {

    /** 偽物の振る舞いです。 */
    public enum Mode {
        /** 承認する。 */
        APPROVE,
        /** 断る。 */
        DECLINE,
        /** 通信に失敗する。 */
        DOWN
    }

    // 振る舞いを持つフィールド
    private final Mode mode;
    // 呼ばれた回数を持つフィールド
    private int calls;

    /**
     * 偽物を作ります。
     *
     * @param mode 振る舞い
     */
    public FakePaymentClient(Mode mode) {
        // 引数の振る舞いを、フィールドに保存する
        this.mode = mode;
    }

    /**
     * 決まった振る舞いで答えます。
     *
     * @param customerId 顧客ID
     * @param amount     金額（円）
     * @return 承認なら true
     * @throws IOException 通信の失敗を選んだとき
     */
    @Override
    public boolean authorize(String customerId, int amount) throws IOException {
        // 呼ばれた回数を数える
        calls++;
        // 通信の失敗を選んだとき
        if (mode == Mode.DOWN) {
            // 通信の失敗を投げる
            throw new IOException("決済会社に接続できません");
        }
        // 承認を選んだときだけ true を返す
        return mode == Mode.APPROVE;
    }

    /** @return 呼ばれた回数 */
    public int calls() { return calls; }
}
