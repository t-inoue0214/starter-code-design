package com.example.orders.ch18_business_classes;

/**
 * 仕様の例です。「指定した商品を含むこと」という1つの条件だけを表します。
 */
public class HasProductSpecification implements OrderSpecification {
    // 含まれていてほしい商品IDを持つフィールド
    private final String productId;

    /**
     * 仕様を作ります。
     *
     * @param productId 含まれていてほしい商品ID
     */
    public HasProductSpecification(String productId) {
        // 引数の商品IDを、フィールドに保存する
        this.productId = productId;
    }

    /**
     * 指定の商品を含むかを判定します。
     *
     * @param order 対象の注文
     * @return 含むなら true
     */
    @Override
    public boolean isSatisfiedBy(PlacedOrder order) {
        // 明細のうち、どれか1つでも商品IDが一致するかを返す
        return order.lines().stream().anyMatch(line -> line.productId().equals(productId));
    }
}
