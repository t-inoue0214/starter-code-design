package com.example.orders.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * 注文を表すイミュータブルなクラスです。
 */
public class Order {
    private final String id;
    private final Customer customer;
    private final List<OrderLine> lines;
    private final String couponCode;
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
        this.id = id;
        this.customer = customer;
        this.lines = lines == null ? null : List.copyOf(lines);
        this.couponCode = couponCode;
        this.orderedOn = orderedOn;
    }

    public String id() { return id; }
    public Customer customer() { return customer; }
    public List<OrderLine> lines() { return lines; }
    public String couponCode() { return couponCode; }
    public LocalDate orderedOn() { return orderedOn; }
}
