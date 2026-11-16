package com.example.orders.ch19_immutability;

/**
 * 月ごとの売上目標です。技法: 配列の防御的コピー（受け取るときと、渡すとき）。
 * 配列は、List.copyOf のような変更できない版がないので、clone でコピーします。
 */
public final class MonthlyTargets {
    // 12か月分の目標（円）を持つフィールド。配列の中身は、このクラスの外に出さない
    private final int[] targets;

    /**
     * 目標を作ります。
     *
     * @param targets 12か月分の目標（円）。呼び出し側があとで変更しても影響しない
     */
    public MonthlyTargets(int[] targets) {
        // 12か月分でないとき
        if (targets.length != 12) {
            // 不正な入力として拒否する
            throw new IllegalArgumentException("目標は12か月分にしてください: " + targets.length);
        }
        // ★重要★ 受け取った配列を、コピーして持つ。final は「参照が変わらない」だけで、中身は守らない
        this.targets = targets.clone();
    }

    /**
     * 指定した月の目標を返します。
     *
     * @param month 月（1〜12）
     * @return その月の目標（円）
     */
    public int target(int month) {
        // 月から添字に直して、目標を返す
        return targets[month - 1];
    }

    /** @return 12か月分の目標のコピー */
    public int[] toArray() {
        // ★重要★ 内部の配列は渡さず、コピーを渡す
        return targets.clone();
    }
}
