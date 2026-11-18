package com.example.orders.ch22_testability.before;

import java.util.ArrayList;
import java.util.List;

/**
 * アプリ全体で1つだけの、クーポンの保存先です。どこからでも INSTANCE で触れます。
 * テストどうしで中身を共有してしまうので、前のテストの結果が、次のテストに残ります。
 */
public final class GlobalCouponStore {
    // アプリ全体で共有する、ただ1つのインスタンス
    public static final GlobalCouponStore INSTANCE = new GlobalCouponStore();
    // 保存したクーポンのコードを持つフィールド
    private final List<String> codes = new ArrayList<>();

    /** 外からは作らせず、INSTANCE だけを使わせます。 */
    private GlobalCouponStore() {
    }

    /**
     * クーポンのコードを保存します。
     *
     * @param code クーポンのコード
     */
    public void save(String code) {
        // 一覧に足す
        codes.add(code);
    }

    /** @return 保存したクーポンの数 */
    public int count() { return codes.size(); }
}
