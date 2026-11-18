package com.example.orders.ch23_naming.before;

/**
 * 名前の付け方が悪い、注文の処理です。振る舞いは、名前を直したクラスと同じです。
 * 呼び出し側の process(data, true) を読んでも、何が起きるのか分かりません。
 */
public class OrderManager {

    /**
     * 注文を処理します。何をするのかは、中を読まないと分かりません。
     *
     * @param data 注文のデータ
     * @param flag 何かの印（実は「会員かどうか」）
     * @return 何かの金額（実は「割引と税を反映した支払額」）
     */
    public int process(OrderData data, boolean flag) {
        // 金額を取り出す
        int a = data.amt();
        // 印が立っているとき
        if (flag) {
            // 5%引く
            a = a * 95 / 100;
        }
        // 税を足して返す
        return a + a * 10 / 100;
    }

    /**
     * 注文を確かめます。何を確かめて、true が何を意味するのかは、中を読まないと分かりません。
     *
     * @param data 注文のデータ
     * @return 何かが成り立てば true（実は「取り消せるなら true」）
     */
    public boolean check(OrderData data) {
        // 状態が "P" で、日数が7以下なら true を返す
        return data.st().equals("P") && data.days() <= 7;
    }
}
