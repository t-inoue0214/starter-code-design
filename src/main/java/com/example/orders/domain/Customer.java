package com.example.orders.domain;

/**
 * 注文者を表すイミュータブルなクラスです。
 */
public class Customer {
    private final String id;
    private final String name;
    private final MemberRank rank;
    private final String email;
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
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.email = email;
        this.phone = phone;
    }

    public String id() { return id; }
    public String name() { return name; }
    public MemberRank rank() { return rank; }
    public String email() { return email; }
    public String phone() { return phone; }
}
