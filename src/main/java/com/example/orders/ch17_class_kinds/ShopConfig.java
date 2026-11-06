package com.example.orders.ch17_class_kinds;

import com.example.orders.domain.OrderValidationException;
import java.util.Map;

/**
 * 設定クラス（Config）の例です。起動時に決まる設定値を、まとめて持ちます。
 * 設定の読み込み元（ファイル、環境変数など）を知らなくても使えるよう、Map から作ります。
 */
public final class ShopConfig {
    // 決済の上限額（円）を持つフィールド
    private final int paymentLimit;
    // 季節割引の割合（%）を持つフィールド
    private final int seasonalRatePercent;

    /**
     * 検証済みの設定値から作ります。外からは defaults か fromMap を使います。
     *
     * @param paymentLimit        決済の上限額（円）
     * @param seasonalRatePercent 季節割引の割合（%）
     */
    private ShopConfig(int paymentLimit, int seasonalRatePercent) {
        // 引数の決済の上限額（円）を、フィールドに保存する
        this.paymentLimit = paymentLimit;
        // 引数の季節割引の割合（%）を、フィールドに保存する
        this.seasonalRatePercent = seasonalRatePercent;
    }

    /** @return 標準の設定（決済上限50万円、季節割引3%） */
    public static ShopConfig defaults() {
        // 標準の設定値（決済上限50万円、季節割引3%）で、設定を作って返す
        return new ShopConfig(500_000, 3);
    }

    /**
     * 設定値の Map から、設定を作ります。指定がない項目は標準値を使います。
     *
     * @param props 設定名と値の Map（例: payment.limit）
     * @return 設定
     */
    public static ShopConfig fromMap(Map<String, String> props) {
        // 指定がないときに使う、標準の設定を取り出す
        ShopConfig base = defaults();
        // 決済上限を読む（指定がなければ標準値）
        int limit = parse(props.get("payment.limit"), base.paymentLimit);
        // 季節割引の割合を読む（指定がなければ標準値）
        int rate = parse(props.get("seasonal.rate"), base.seasonalRatePercent);
        // ★重要★ 設定値が範囲外のとき（起動時に不正な設定を見つける）
        if (limit < 0 || rate < 0 || rate > 100) {
            // 範囲外の設定は、検証エラーとして投げる
            throw new OrderValidationException("設定値が範囲外です");
        }
        // 読み込んだ設定値から、設定を作って返す
        return new ShopConfig(limit, rate);
    }

    /**
     * 設定値の文字列を数値に直します。指定がなければ、代わりの値を返します。
     *
     * @param text     設定値の文字列（なければ null）
     * @param fallback 指定がないときの値
     * @return 数値に直した設定値
     */
    private static int parse(String text, int fallback) {
        // 指定がなければ標準値、あれば数値に直して返す
        return text == null ? fallback : Integer.parseInt(text.trim());
    }

    /** @return 決済の上限額（円） */
    public int paymentLimit() { return paymentLimit; }

    /** @return 季節割引の割合（%） */
    public int seasonalRatePercent() { return seasonalRatePercent; }
}
