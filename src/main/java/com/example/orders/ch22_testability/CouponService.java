package com.example.orders.ch22_testability;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

/**
 * 注文のあとにクーポンを発行するサービスです。
 * 時計、コードの作り方、保存先を、すべてコンストラクタで受け取ります。中で直接は作りません。
 */
public final class CouponService {
    // 今の時刻を教える時計を持つフィールド
    private final Clock clock;
    // コードを作る部品を持つフィールド
    private final CodeGenerator codes;
    // 保存先を持つフィールド
    private final CouponRepository repository;
    // クーポンの決まりを持つフィールド
    private final CouponRule rule;

    /**
     * サービスを作ります。
     *
     * @param clock      時計（本番は今の時刻、テストは決まった時刻）
     * @param codes      コードを作る部品
     * @param repository 保存先
     * @param rule       クーポンの決まり
     */
    public CouponService(Clock clock, CodeGenerator codes, CouponRepository repository, CouponRule rule) {
        // ★重要★ 変わるもの、外にあるものは、すべて外から受け取る。テストでは偽物を渡せる
        this.clock = clock;
        // 引数のコードの部品を、フィールドに保存する
        this.codes = codes;
        // 引数の保存先を、フィールドに保存する
        this.repository = repository;
        // 引数の決まりを、フィールドに保存する
        this.rule = rule;
    }

    /**
     * 条件を満たせば、クーポンを発行して保存します。
     *
     * @param customerId 顧客ID
     * @param orderTotal 注文の合計（円）
     * @return 発行したクーポン。発行しなければ空
     */
    public Optional<Coupon> issue(String customerId, int orderTotal) {
        // ★重要★ 今日の日付は、受け取った時計から取る。LocalDate.now() を引数なしで呼ばない
        LocalDate today = LocalDate.now(clock);
        // 決まりに、割引額を聞く
        int discount = rule.discountFor(orderTotal, today);
        // 割引がないとき
        if (discount == 0) {
            // 発行しないことを、空で返す
            return Optional.empty();
        }
        // コードを作り、クーポンを組み立てる
        Coupon coupon = new Coupon(codes.next(), customerId, discount, rule.expiryFrom(today));
        // 保存先に保存する
        repository.save(coupon);
        // 発行したクーポンを返す
        return Optional.of(coupon);
    }
}
