package com.example.orders.ch22_testability;

import java.util.UUID;

/**
 * 本番で使う、乱数でコードを作る部品です。
 */
public final class RandomCodeGenerator implements CodeGenerator {

    /**
     * 乱数で、重ならないコードを作ります。
     *
     * @return 新しいコード
     */
    @Override
    public String next() {
        // 乱数のIDを文字列にして返す
        return UUID.randomUUID().toString();
    }
}
