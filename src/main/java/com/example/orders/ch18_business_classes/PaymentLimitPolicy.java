package com.example.orders.ch18_business_classes;

/**
 * ポリシーの例です。「1回の決済額が上限を超えないか」だけを判定します。
 * 上限はコンストラクタで受け取るので、顧客ごとに違う上限も、同じクラスで表せます。
 */
public class PaymentLimitPolicy {
    // 決済額の上限（円）を持つフィールド
    private final int limit;

    /**
     * ポリシーを作ります。
     *
     * @param limit 決済額の上限（円）。この金額ちょうどは許可される
     */
    public PaymentLimitPolicy(int limit) {
        // ★重要★ 顧客ごとの違いは、クラスを増やさず、値（上限）として外から渡す
        this.limit = limit;
    }

    /**
     * 決済額が上限以内かを判定します。判定だけで、例外を投げたり、状態を変えたりしません。
     *
     * @param total 決済額（円）
     * @return 上限以内なら許可、超えていれば理由付きの不許可
     */
    public PolicyDecision check(int total) {
        // 決済額が上限を超えているとき
        if (total > limit) {
            // ★重要★ 「どう扱うか」は決めず、理由付きの不許可を返すだけにする
            return PolicyDecision.deny("決済額が上限を超えています: " + total);
        }
        // 上限以内なので、許可を返す
        return PolicyDecision.allow();
    }
}
