package com.example.orders.ch23_naming;

/**
 * 注文の状態です。業務で使う言葉を、そのまま名前にしています。
 */
public enum OrderStatus {
    /** 確定済み。 */
    PLACED,
    /** 出荷済み。 */
    SHIPPED
}
