package com.example.orders.ch22_testability;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * クーポンの決まりです。日付を引数で受け取るので、同じ引数なら、いつ呼んでも同じ答えを返します。
 * 時計も乱数も保存先も使わないので、テストでは値を渡して答えを確かめるだけで済みます。
 */
public final class CouponRule {
    // 発行する合計の下限（円）
    static final int THRESHOLD = 5000;
    // 平日の割引額（円）
    static final int WEEKDAY_DISCOUNT = 500;
    // 有効期間（日）
    static final int VALID_DAYS = 30;

    /**
     * 割引額を決めます。
     *
     * @param orderTotal 注文の合計（円）
     * @param today      今日の日付（呼び出し側が渡す）
     * @return 割引額（円）。発行しないなら0
     */
    public int discountFor(int orderTotal, LocalDate today) {
        // 合計が下限に届かないとき
        if (orderTotal < THRESHOLD) {
            // 発行しない
            return 0;
        }
        // 曜日を取る
        DayOfWeek day = today.getDayOfWeek();
        // ★重要★ 日付は引数でもらう。「今日」を自分で取りに行かないので、どの曜日でもテストできる
        boolean weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        // 週末なら2倍、それ以外は平日の額を返す
        return weekend ? WEEKDAY_DISCOUNT * 2 : WEEKDAY_DISCOUNT;
    }

    /**
     * 有効期限を決めます。
     *
     * @param issuedOn 発行日
     * @return 有効期限
     */
    public LocalDate expiryFrom(LocalDate issuedOn) {
        // 発行日に有効期間を足して返す
        return issuedOn.plusDays(VALID_DAYS);
    }
}
