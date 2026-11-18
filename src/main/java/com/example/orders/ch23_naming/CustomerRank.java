package com.example.orders.ch23_naming;

/**
 * 顧客の区分です。boolean の引数の代わりに使います。
 * 呼び出し側に CustomerRank.MEMBER と書かれるので、何を渡したかが読めます。
 */
public enum CustomerRank {
    /** 一般の顧客。 */
    REGULAR,
    /** 会員。割引を受けられる。 */
    MEMBER
}
