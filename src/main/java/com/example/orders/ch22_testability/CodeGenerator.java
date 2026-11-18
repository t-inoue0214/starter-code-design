package com.example.orders.ch22_testability;

/**
 * クーポンのコードを作る部品です。本番では乱数で作り、テストでは決まった順に作る偽物に差し替えます。
 */
public interface CodeGenerator {

    /**
     * 次のコードを作ります。
     *
     * @return 新しいコード
     */
    String next();
}
