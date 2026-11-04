package com.example.orders.domain;

/**
 * 注文者を表すイミュータブルなクラスです。
 */
public class Customer {
    // 注文IDを持つフィールド
    private final String id;
    // nameを持つフィールド
    private final String name;
    // 会員ランクを持つフィールド
    private final MemberRank rank;
    // メールアドレスを持つフィールド
    private final String email;
    // 電話番号を持つフィールド
    private final String phone;

    /**
     * 注文者を生成します。
     *
     * @param id    顧客ID
     * @param name  氏名
     * @param rank  会員ランク
     * @param email メールアドレス（通知不要なら null）
     * @param phone 電話番号（SMS通知不要なら null）
     */
    public Customer(String id, String name, MemberRank rank, String email, String phone) {
        // 引数の注文IDを、フィールドに保存する
        this.id = id;
        // 引数のnameを、フィールドに保存する
        this.name = name;
        // 引数の会員ランクを、フィールドに保存する
        this.rank = rank;
        // 引数のメールアドレスを、フィールドに保存する
        this.email = email;
        // 引数の電話番号を、フィールドに保存する
        this.phone = phone;
    }

    // 注文IDを返す
    public String id() { return id; }
    // nameを返す
    public String name() { return name; }
    // 会員ランクを返す
    public MemberRank rank() { return rank; }
    // メールアドレスを返す
    public String email() { return email; }
    // 電話番号を返す
    public String phone() { return phone; }
}
