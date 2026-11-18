package com.example.orders.ch23_naming.before;

/**
 * 名前の付け方が悪い、注文のデータです。振る舞いは、名前を直した Order と同じです。
 * 「Data」は何でも入れられる名前で、フィールドの名前は省略形です。
 */
public class OrderData {
    // 状態を持つフィールド（"P" は確定済み、"S" は出荷済み。名前からは分からない）
    private final String st;
    // 金額を持つフィールド（税込みか税抜きか、名前からは分からない）
    private final int amt;
    // 日数を持つフィールド（何からの日数か、名前からは分からない）
    private final int days;

    /**
     * データを作ります。
     *
     * @param st   状態
     * @param amt  金額
     * @param days 日数
     */
    public OrderData(String st, int amt, int days) {
        // 引数の状態を、フィールドに保存する
        this.st = st;
        // 引数の金額を、フィールドに保存する
        this.amt = amt;
        // 引数の日数を、フィールドに保存する
        this.days = days;
    }

    /** @return 状態 */
    public String st() { return st; }

    /** @return 金額 */
    public int amt() { return amt; }

    /** @return 日数 */
    public int days() { return days; }
}
