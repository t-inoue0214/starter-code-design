package com.example.orders.ch22_testability.before;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 注文のあとにクーポンを発行する処理です。今日の日付、乱数、共有の保存先を、中で直接使っています。
 * そのため、テストで結果を決め打ちできません。
 */
public class HardToTestCouponService {

    /**
     * 合計が5000円以上ならクーポンを発行し、割引額を返します。
     * 週末は割引額が2倍です。発行しなければ0を返します。
     *
     * @param customerId 顧客ID
     * @param orderTotal 注文の合計（円）
     * @return 割引額（円）
     */
    public int issue(String customerId, int orderTotal) {
        // 合計が5000円未満のとき
        if (orderTotal < 5000) {
            // 発行しない
            return 0;
        }
        // 今日の日付を、中で直接取る（テストのたびに変わる）
        LocalDate today = LocalDate.now();
        // 曜日を取る
        DayOfWeek day = today.getDayOfWeek();
        // 週末なら2倍、それ以外は500円にする
        int discount = (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) ? 1000 : 500;
        // コードを乱数で作る（テストのたびに変わる）
        String code = customerId + "-" + UUID.randomUUID();
        // 共有の保存先に、直接保存する（テストどうしで中身が残る）
        GlobalCouponStore.INSTANCE.save(code);
        // 割引額を返す
        return discount;
    }
}
