package com.example.orders.ch17_class_kinds;

/**
 * ユーティリティ（Util / Helper）の例です。状態を持たない、便利な静的メソッドの集まりです。
 * インスタンスは作らず、クラス名から直接呼びます。
 */
public final class YenFormat {
    /** 静的メソッドだけのクラスなので、インスタンスは作りません。 */
    private YenFormat() {
    }

    /**
     * 金額を、3桁区切りの円表記にします。
     *
     * @param yen 金額（円）
     * @return 例: 1,234円
     */
    public static String format(int yen) {
        // 3桁区切りの文字列に直して返す
        return String.format("%,d円", yen);
    }
}
