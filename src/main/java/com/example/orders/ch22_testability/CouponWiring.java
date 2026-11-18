package com.example.orders.ch22_testability;

import java.time.Clock;

/**
 * 本番用の部品を組み立てる場所です。本物の時計と乱数は、ここでだけ選びます。
 */
public final class CouponWiring {

    /** 組み立てるだけのクラスなので、インスタンスは作りません。 */
    private CouponWiring() {
    }

    /**
     * 本番用のサービスを組み立てます。
     *
     * @param repository 保存先
     * @return 本番用のサービス
     */
    public static CouponService production(CouponRepository repository) {
        // ★重要★ 本物の時計と乱数を選ぶのは、組み立ての場所だけ。サービスの中は、どちらか知らない
        return new CouponService(Clock.systemDefaultZone(), new RandomCodeGenerator(), repository, new CouponRule());
    }
}
