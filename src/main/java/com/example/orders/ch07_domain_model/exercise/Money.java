package com.example.orders.ch07_domain_model.exercise;

/**
 * 金額を表す値オブジェクト（Value Object）です。
 * TODO: 次の仕様で実装してください（テストがこの形を前提にしています）。
 *   - 不変（フィールドは final。計算は新しい Money を返す）
 *   - 負の金額は作れない（of に負の値を渡す、minus の結果が負になる場合は IllegalArgumentException）
 *   - 金額が同じなら equals / hashCode が等しい
 *   - percent は円未満を切り捨てる（例: 999円の3% は 29円）
 */
public final class Money {
    /**
     * @param yen 金額（円）
     * @return Money
     */
    public static Money of(int yen) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /** @return 0円 */
    public static Money zero() {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /** @return 金額（円） */
    public int yen() {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param other 足す金額
     * @return 合計
     */
    public Money plus(Money other) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param other 引く金額
     * @return 差
     */
    public Money minus(Money other) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param quantity 数量
     * @return 数量倍した金額
     */
    public Money times(int quantity) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param rate 割合（%）
     * @return 割合を掛けた金額（円未満は切り捨て）
     */
    public Money percent(int rate) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param other 比べる金額
     * @return この金額のほうが大きければ true
     */
    public boolean isGreaterThan(Money other) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }

    /**
     * @param other 比べる金額
     * @return 小さいほうの金額
     */
    public Money min(Money other) {
        throw new UnsupportedOperationException("TODO: 第7章の課題です");
    }
}
