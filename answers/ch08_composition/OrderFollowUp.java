package com.example.orders.ch08_composition.exercise;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderResult;

/**
 * 決済のあとに行う追加の処理（通知、監査ログなど）を表します。
 * 課題では、継承で足していた処理を、この型の部品として持たせ直します。
 */
public interface OrderFollowUp {
    /**
     * 追加の処理を行います。
     *
     * @param order  処理した注文
     * @param result 処理の結果（金額の内訳）
     */
    void run(Order order, OrderResult result);
}
