package com.example.orders.ch17_class_kinds;

/**
 * 定数クラスの例です。変わらない上限値に、名前を付けてまとめます。
 * 値の意味が名前で伝わり、数値が複数の場所に散らばるのを防ぎます。
 */
public final class OrderLimits {
    /** 1つの明細で注文できる最大数量。 */
    public static final int MAX_QUANTITY_PER_LINE = 99;

    /** 1回の注文に含められる最大の明細数。 */
    public static final int MAX_LINES_PER_ORDER = 20;

    /** 定数をまとめるだけのクラスなので、インスタンスは作りません。 */
    private OrderLimits() {
    }
}
