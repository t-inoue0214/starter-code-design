package com.example.orders.ch19_immutability.before;

/**
 * 配列をそのまま持つ、月ごとの売上目標です。フィールドは final ですが、
 * final が守るのは「どの配列を指すか」だけで、配列の中身は外から変えられます。
 */
public class LeakyMonthlyTargets {
    // 12か月分の目標を持つフィールド（受け取った配列そのもの）
    private final int[] targets;

    /**
     * 目標を作ります。
     *
     * @param targets 12か月分の目標（円）
     */
    public LeakyMonthlyTargets(int[] targets) {
        // 受け取った配列を、そのまま保存する（呼び出し側と同じ配列を共有してしまう）
        this.targets = targets;
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
}
