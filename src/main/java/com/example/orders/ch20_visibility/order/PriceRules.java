package com.example.orders.ch20_visibility.order;

/**
 * 行の金額と上限の決まりです。注文のパッケージの中だけで使うので、public を付けていません。
 * 外から見えないので、計算の仕方を変えても、外のコードには影響しません。
 */
final class PriceRules {
    // ★重要★ 上限も外に見せない。外は Order の addItem が拒否するかどうかだけを知ればよい
    static final int MAX_QUANTITY = 99;

    /** 計算だけのクラスなので、インスタンスは作りません。 */
    private PriceRules() {
    }

    /**
     * 行の小計を計算します。
     *
     * @param unitPrice 単価（円）
     * @param quantity  数量
     * @return 小計（円）
     */
    static int subtotal(int unitPrice, int quantity) {
        // 単価に数量を掛けて返す
        return unitPrice * quantity;
    }

    /**
     * 数量が上限の範囲に収まっているかを確かめます。
     *
     * @param quantity 確かめる数量
     */
    static void checkQuantity(int quantity) {
        // 数量が1未満か、上限を超えるとき
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            // 範囲外なので拒否する
            throw new IllegalArgumentException("数量は1〜" + MAX_QUANTITY + "です: " + quantity);
        }
    }
}
