package com.example.orders.ch17_class_kinds;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum（列挙型）の例です。注文の状態という、取りうる値が決まっている区分を表します。
 * 状態ごとの振る舞い（取り消せるか）を持てます。
 * また、DB や外部システムの値（数値のコード、文字列の名前）との変換も、この Enum が引き受けます。
 */
public enum OrderStatus {
    /** 受け付けた状態。まだ取り消せる。 */
    PLACED(1, "受付済み", true),
    /** 出荷した状態。もう取り消せない。 */
    SHIPPED(2, "出荷済み", false),
    /** 取り消した状態。DB のコードは、あえて連番にしていない。 */
    CANCELLED(9, "取消済み", false);

    // BY_CODEを持つフィールド
    private static final Map<Integer, OrderStatus> BY_CODE = new HashMap<>();

    // ★重要★ クラスの読み込み時に1回だけ実行して、コードから Enum を引く表を作る
    static {
        // すべての状態を、1つずつ取り出す
        for (OrderStatus status : values()) {
            // DB のコードを鍵にして、状態を表に登録する
            BY_CODE.put(status.code, status);
        }
    }

    // クーポンコードを持つフィールド
    private final int code;
    // labelを持つフィールド
    private final String label;
    // cancellableを持つフィールド
    private final boolean cancellable;

    /**
     * 状態ごとの DB のコードと、表示名と、取り消せるかどうかを設定します。
     *
     * @param code        DB に保存する数値のコード（並び順を変えても変わらない値）
     * @param label       画面に出す表示名
     * @param cancellable 取り消せる状態なら true
     */
    OrderStatus(int code, String label, boolean cancellable) {
        // 引数のクーポンコードを、フィールドに保存する
        this.code = code;
        // 引数のlabelを、フィールドに保存する
        this.label = label;
        // 引数のcancellableを、フィールドに保存する
        this.cancellable = cancellable;
    }

    /**
     * DB の数値のコードから、Enum を取り出します。
     *
     * @param code DB に保存されていた数値のコード
     * @return 対応する状態
     * @throws IllegalArgumentException 対応する状態がないとき
     */
    public static OrderStatus fromCode(int code) {
        // 表から、コードに対応する状態を探す
        OrderStatus status = BY_CODE.get(code);
        // 対応する状態がなかったとき
        if (status == null) {
            // ★重要★ 未知のコードは、黙って別の値にせず、例外で知らせる
            throw new IllegalArgumentException("未知の状態コードです: " + code);
        }
        // 見つかった状態を返す
        return status;
    }

    /**
     * 文字列の名前から、Enum を取り出します。前後の空白と、大文字小文字の違いは吸収します。
     *
     * @param name 状態の名前（例: SHIPPED）
     * @return 対応する状態
     * @throws IllegalArgumentException 名前が空か、対応する状態がないとき
     */
    public static OrderStatus fromName(String name) {
        // 名前が空のとき
        if (name == null || name.isBlank()) {
            // 空の名前は、変換できないので例外にする
            throw new IllegalArgumentException("状態の名前がありません");
        }
        // 例外が起こりうる処理を、ここから試す
        try {
            // 空白を取り除き、大文字にそろえてから、名前で Enum を探す
            return valueOf(name.trim().toUpperCase());
        // 列挙型に直せなかったとき
        } catch (IllegalArgumentException e) {
            // valueOf の例外を、分かりやすいメッセージに包んで投げ直す
            throw new IllegalArgumentException("未知の状態名です: " + name, e);
        }
    }

    /** @return DB に保存する数値のコード */
    public int code() { return code; }

    /** @return 画面に出す表示名 */
    public String label() { return label; }

    /** @return 取り消せる状態なら true */
    public boolean canCancel() { return cancellable; }
}
