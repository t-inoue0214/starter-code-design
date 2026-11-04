package com.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文を表すイミュータブルなクラスです。
 */
public class Order {
    // 注文IDを持つフィールド
    private final String id;
    // 顧客を持つフィールド
    private final Customer customer;
    // 明細を持つフィールド
    private final List<OrderLine> lines;
    // couponCodeを持つフィールド
    private final String couponCode;
    // orderedOnを持つフィールド
    private final LocalDate orderedOn;

    /**
     * 注文を生成します。
     *
     * @param id         注文ID
     * @param customer   注文者
     * @param lines      明細（防御的コピーされます）
     * @param couponCode クーポンコード（なければ null）
     * @param orderedOn  注文日
     */
    public Order(String id, Customer customer, List<OrderLine> lines, String couponCode, LocalDate orderedOn) {
        // 引数の注文IDを、フィールドに保存する
        this.id = id;
        // 引数の顧客を、フィールドに保存する
        this.customer = customer;
        // 明細を、書き換えられないコピーとして保存する（null はそのまま）
        this.lines = lines == null ? null : List.copyOf(lines);
        // 引数のcouponCodeを、フィールドに保存する
        this.couponCode = couponCode;
        // 引数のorderedOnを、フィールドに保存する
        this.orderedOn = orderedOn;
    }

    // 注文IDを返す
    public String id() { return id; }
    // 顧客を返す
    public Customer customer() { return customer; }
    // 明細を返す
    public List<OrderLine> lines() { return lines; }
    // couponCodeを返す
    public String couponCode() { return couponCode; }
    // orderedOnを返す
    public LocalDate orderedOn() { return orderedOn; }
}
