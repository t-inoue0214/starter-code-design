package com.example.orders.ch22_testability;

/**
 * テスト用の、決まった順にコードを作る偽物（スタブ）です。C-0001、C-0002 の順に返します。
 */
final class SequenceCodeGenerator implements CodeGenerator {
    // 作った数を持つフィールド
    private int count;

    /**
     * 次の番号のコードを作ります。
     *
     * @return 新しいコード
     */
    @Override
    public String next() {
        // 作った数を増やす
        count++;
        // 4けたの番号つきのコードを返す
        return String.format("C-%04d", count);
    }
}
