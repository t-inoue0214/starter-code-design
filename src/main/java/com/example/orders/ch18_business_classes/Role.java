package com.example.orders.ch18_business_classes;

/**
 * 操作する人の役割を表す Enum です。権限のルール（認可ポリシー）が、この値で分岐します。
 */
public enum Role {
    /** 注文した本人（顧客）。 */
    CUSTOMER,
    /** 運用担当者。他人の注文も操作できる。 */
    ADMIN
}
