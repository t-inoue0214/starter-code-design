package com.example.orders.ch18_business_classes;

/**
 * 仕様の例です。「小計が下限以上であること」という1つの条件だけを表します。
 */
public class MinimumAmountSpecification implements OrderSpecification {
    // 小計の下限（円）を持つフィールド
    private final int minimum;

    /**
     * 仕様を作ります。
     *
     * @param minimum 小計の下限（円）。この金額ちょうどは満たす
     */
    public MinimumAmountSpecification(int minimum) {
        // 引数の下限を、フィールドに保存する
        this.minimum = minimum;
    }

    /**
     * 小計が下限以上かを判定します。
     *
     * @param order 対象の注文
     * @return 下限以上なら true
     */
    @Override
    public boolean isSatisfiedBy(PlacedOrder order) {
        // 小計が下限以上かどうかを返す
        return order.subtotal() >= minimum;
    }
}
