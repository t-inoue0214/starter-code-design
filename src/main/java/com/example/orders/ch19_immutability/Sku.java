package com.example.orders.ch19_immutability;

/**
 * イミュータブルな商品コードです。セッターを持たないので、Set のキーにしても安全です。
 * 比べるための before/MutableSku は、セッターを持つため、Set の中で迷子になります。
 */
public final class Sku {
    // 商品コードを持つフィールド
    private final String code;

    /**
     * 商品コードを作ります。前後の空白を除き、大文字にそろえます。
     *
     * @param code 商品コード
     */
    public Sku(String code) {
        // ★重要★ 正規化は、作る時点で1回だけ行う。あとから変わらないので、いつ比べても同じ結果
        this.code = code.trim().toUpperCase();
    }

    /** @return 商品コード */
    public String code() { return code; }

    /**
     * 商品コードが同じなら等しいとみなします。
     *
     * @param o 比べる相手
     * @return 等しければ true
     */
    @Override
    public boolean equals(Object o) {
        // 相手も商品コードで、文字列が同じなら等しい
        return o instanceof Sku && ((Sku) o).code.equals(code);
    }

    /** @return 商品コードから計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 商品コードから計算したハッシュ値を返す（変わらないので、ずっと同じ値）
        return code.hashCode();
    }
}
