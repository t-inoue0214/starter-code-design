package com.example.orders.ch17_class_kinds;

import com.example.orders.domain.OrderValidationException;

/**
 * Value Object（値オブジェクト）の例です。メールアドレスという「値」を表します。
 * 不変で、値が同じなら等しく、作った時点で正しいことが保証されます。
 */
public final class EmailAddress {
    // 値を持つフィールド
    private final String value;

    /**
     * 検証済みの値から作ります。外からは、検証を行う of を使います。
     *
     * @param value 検証と正規化を終えたメールアドレス
     */
    private EmailAddress(String value) {
        // 引数の値を、フィールドに保存する
        this.value = value;
    }

    /**
     * 文字列からメールアドレスを作ります。形式が不正なら作れません。
     *
     * @param text メールアドレスの文字列
     * @return メールアドレス
     */
    public static EmailAddress of(String text) {
        // メールアドレスが空のとき
        if (text == null || text.isBlank()) {
            // 空のメールアドレスは作れないので、検証エラーを投げる
            throw new OrderValidationException("メールアドレスがありません");
        }
        // 前後の空白を取り除き、小文字にそろえる（同じ値は同じ表記にする）
        String normalized = text.trim().toLowerCase();
        // @ の位置を調べる
        int at = normalized.indexOf('@');
        // @ がない、または @ が先頭か末尾にあるとき
        if (at <= 0 || at == normalized.length() - 1) {
            // ★重要★ 不正な値は、作る時点で拒否する（作れたものは必ず正しい）
            throw new OrderValidationException("メールアドレスの形式が不正です: " + text);
        }
        // 正しいと確かめた値から、メールアドレスを作って返す
        return new EmailAddress(normalized);
    }

    /** @return 小文字にそろえたメールアドレス */
    public String value() { return value; }

    /** @return @ より後ろのドメイン部分 */
    public String domain() {
        // @ より後ろの部分（ドメイン）を取り出して返す
        return value.substring(value.indexOf('@') + 1);
    }

    /**
     * 値が同じなら、等しいとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // ★重要★ 値が同じなら等しい（Value Object は値で比べる）
        return o instanceof EmailAddress && ((EmailAddress) o).value.equals(value);
    }

    /** @return 値から計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 値から計算したハッシュ値を返す（equals と整合させる）
        return value.hashCode();
    }

    /** @return メールアドレスの文字列 */
    @Override
    public String toString() {
        // メールアドレスの文字列を返す
        return value;
    }
}
