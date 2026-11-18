package com.example.orders.ch22_testability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch22_testability.before.GlobalCouponStore;
import com.example.orders.ch22_testability.before.HardToTestCouponService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * テストしやすい設計を、動かして確かめるテストです。
 * before の「時計、乱数、共有の保存先を中で使う実装」と、外から受け取る実装を比べます。
 * このテストは、最初から全て green です。
 */
class TestabilityTest {
    // テストで使う時間帯
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    /**
     * 決まった日の正午で止まった時計を作る補助メソッドです。
     *
     * @param date 日付
     * @return 止まった時計
     */
    private static Clock fixedAt(LocalDate date) {
        // その日の正午で止まった時計を作って返す
        return Clock.fixed(ZonedDateTime.of(date.atTime(12, 0), TOKYO).toInstant(), TOKYO);
    }

    /**
     * 決まった日の時計と偽物の部品で、サービスを作る補助メソッドです。
     *
     * @param date       日付
     * @param repository 保存先
     * @return サービス
     */
    private static CouponService serviceOn(LocalDate date, CouponRepository repository) {
        // 止まった時計、順番のコード、渡された保存先で組み立てて返す
        return new CouponService(fixedAt(date), new SequenceCodeGenerator(), repository, new CouponRule());
    }

    /** 今日に依存すると、期待値を1つに決められない。 */
    @Test
    @DisplayName("before は今日の曜日で結果が変わるので、「500か1000のどちらか」としか書けない")
    void beforeDependsOnToday() {
        // before のサービスで発行する
        int discount = new HardToTestCouponService().issue("C001", 5000);
        // 期待値を1つに決められず、弱い確かめ方しかできない
        assertTrue(discount == 500 || discount == 1000);
    }

    /** 共有の保存先は、テストの間で中身が残る。 */
    @Test
    @DisplayName("before は共有の保存先を使うので、件数が、それまでに動いたテストに左右される")
    void beforeSharesGlobalState() {
        // 発行する前の件数を覚えておく（0とは限らない）
        int before = GlobalCouponStore.INSTANCE.count();
        // before のサービスで発行する
        new HardToTestCouponService().issue("C001", 5000);
        // 「1件になった」とは書けず、「1件増えた」としか書けない
        assertEquals(before + 1, GlobalCouponStore.INSTANCE.count());
    }

    /** 計算だけの決まりは、値を渡すだけで確かめられる。 */
    @Test
    @DisplayName("CouponRule は、日付を渡すだけで、平日、週末、下限未満を確かめられる")
    void ruleIsPure() {
        // 決まりを作る
        CouponRule rule = new CouponRule();
        // 金曜日は500円
        assertEquals(500, rule.discountFor(5000, LocalDate.of(2026, 11, 20)));
        // 土曜日は1000円
        assertEquals(1000, rule.discountFor(5000, LocalDate.of(2026, 11, 21)));
        // 下限に届かなければ0円
        assertEquals(0, rule.discountFor(4999, LocalDate.of(2026, 11, 21)));
        // 有効期限は30日後
        assertEquals(LocalDate.of(2026, 12, 20), rule.expiryFrom(LocalDate.of(2026, 11, 20)));
    }

    /** 時計とコードを決め打ちすれば、結果を1つに決められる。 */
    @Test
    @DisplayName("止まった時計と順番のコードを渡せば、発行されるクーポンを丸ごと決め打ちできる")
    void serviceWithFixedClock() {
        // メモリ上の保存先を用意する
        InMemoryCouponRepository repository = new InMemoryCouponRepository();
        // 土曜日の時計で発行する
        Coupon coupon = serviceOn(LocalDate.of(2026, 11, 21), repository).issue("C001", 8000).orElseThrow();
        // コードは決まった値
        assertEquals("C-0001", coupon.code());
        // 週末なので1000円
        assertEquals(1000, coupon.discount());
        // 有効期限は30日後
        assertEquals(LocalDate.of(2026, 12, 21), coupon.expiresOn());
        // 保存先に1件だけある
        assertEquals(1, repository.findByCustomer("C001").size());
    }

    /** 発行しないときは、何も保存しない。 */
    @Test
    @DisplayName("下限に届かなければ空を返し、何も保存しない")
    void noCouponUnderThreshold() {
        // メモリ上の保存先を用意する
        InMemoryCouponRepository repository = new InMemoryCouponRepository();
        // 下限未満で発行を試す
        Optional<Coupon> coupon = serviceOn(LocalDate.of(2026, 11, 21), repository).issue("C001", 4999);
        // 空が返る
        assertTrue(coupon.isEmpty());
        // 何も保存されていない
        assertTrue(repository.findByCustomer("C001").isEmpty());
    }

    /** 保存先をテストごとに作れば、中身が混ざらない。 */
    @Test
    @DisplayName("保存先をテストごとに作るので、別のサービスの結果が混ざらない")
    void repositoriesAreIndependent() {
        // 1つ目の保存先を用意する
        InMemoryCouponRepository first = new InMemoryCouponRepository();
        // 2つ目の保存先を用意する
        InMemoryCouponRepository second = new InMemoryCouponRepository();
        // 1つ目のサービスで発行する
        serviceOn(LocalDate.of(2026, 11, 20), first).issue("C001", 5000);
        // 1つ目には1件ある
        assertEquals(1, first.findByCustomer("C001").size());
        // 2つ目には何もない
        assertTrue(second.findByCustomer("C001").isEmpty());
    }

    /** 本番用の組み立ても動く。 */
    @Test
    @DisplayName("本番用の組み立ては、本物の時計と乱数で発行できる")
    void productionWiringWorks() {
        // 本番用のサービスを組み立てて、発行する
        Coupon coupon = CouponWiring.production(new InMemoryCouponRepository()).issue("C001", 5000).orElseThrow();
        // コードは乱数なので、値は決め打ちせず、空でないことだけ確かめる
        assertFalse(coupon.code().isEmpty());
    }
}
