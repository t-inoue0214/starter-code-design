package com.example.orders.ch18_business_classes;

import java.time.LocalDate;

/**
 * 計算クラスの例です。金額と日付を受け取り、消費税額を返すだけです。
 * 法律で決まる税率は、変わりやすいので、この1クラスに閉じ込めます。
 * 状態を持たず、副作用もないので、同じ入力なら必ず同じ結果になります。
 */
public class TaxCalculator {
    // 税率が変わった日（この日から新しい税率）
    private static final LocalDate RATE_CHANGED_ON = LocalDate.of(2019, 10, 1);

    /**
     * 注文日に適用される税率を返します。
     *
     * @param date 注文日
     * @return 税率（パーセント）
     */
    public int ratePercent(LocalDate date) {
        // ★重要★ 法律の変更点は、ここだけを直せば済むように、1か所にまとめる
        return date.isBefore(RATE_CHANGED_ON) ? 8 : 10;
    }

    /**
     * 消費税額を計算します。円未満は切り捨てます。
     *
     * @param amount 税抜の金額（円）
     * @param date   注文日
     * @return 消費税額（円）
     */
    public int taxOf(int amount, LocalDate date) {
        // 金額に税率を掛けて100で割り、円未満を切り捨てた税額を返す
        return amount * ratePercent(date) / 100;
    }
}
