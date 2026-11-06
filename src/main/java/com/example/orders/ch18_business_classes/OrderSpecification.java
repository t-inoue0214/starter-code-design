package com.example.orders.ch18_business_classes;

/**
 * 仕様（Specification）の窓口です。「注文がある条件を満たすか」だけを判定します。
 * ポリシーの一種ですが、1条件を1クラスにして、and / or / not で組み合わせられる点が特徴です。
 */
public interface OrderSpecification {

    /**
     * 注文が条件を満たすかを判定します。
     *
     * @param order 対象の注文
     * @return 満たすなら true
     */
    boolean isSatisfiedBy(PlacedOrder order);

    /**
     * 「この条件、かつ相手の条件」を表す新しい仕様を作ります。
     *
     * @param other 相手の条件
     * @return 両方を満たすときだけ true になる仕様
     */
    default OrderSpecification and(OrderSpecification other) {
        // ★重要★ 条件を部品にしておくので、組み合わせは新しいクラスなしで作れる
        return order -> isSatisfiedBy(order) && other.isSatisfiedBy(order);
    }

    /**
     * 「この条件、または相手の条件」を表す新しい仕様を作ります。
     *
     * @param other 相手の条件
     * @return どちらかを満たせば true になる仕様
     */
    default OrderSpecification or(OrderSpecification other) {
        // どちらかを満たせば true を返す仕様を作る
        return order -> isSatisfiedBy(order) || other.isSatisfiedBy(order);
    }

    /**
     * 「この条件ではない」を表す新しい仕様を作ります。
     *
     * @return 満たさないときだけ true になる仕様
     */
    default OrderSpecification not() {
        // 結果を反転した仕様を作る
        return order -> !isSatisfiedBy(order);
    }
}
