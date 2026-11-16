package com.example.orders.ch19_immutability.before;

/**
 * セッターを持つ商品コードです。equals と hashCode がコードで決まるのに、コードを変えられます。
 * Set に入れたあとでコードを変えると、Set の中で迷子になります。
 */
public class MutableSku {
    // 商品コードを持つフィールド（あとから変えられる）
    private String code;

    /**
     * 商品コードを作ります。
     *
     * @param code 商品コード
     */
    public MutableSku(String code) {
        // 引数の商品コードを、フィールドに保存する
        this.code = code;
    }

    /**
     * 商品コードを変えます。
     *
     * @param code 新しい商品コード
     */
    public void changeCode(String code) {
        // フィールドを書き換える（ハッシュ値も変わってしまう）
        this.code = code;
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
        return o instanceof MutableSku && ((MutableSku) o).code.equals(code);
    }

    /** @return 商品コードから計算したハッシュ値 */
    @Override
    public int hashCode() {
        // 商品コードから計算したハッシュ値を返す（コードが変わると、この値も変わる）
        return code.hashCode();
    }
}
