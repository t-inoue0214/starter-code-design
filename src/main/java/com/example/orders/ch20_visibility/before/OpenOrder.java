package com.example.orders.ch20_visibility.before;

import java.util.ArrayList;
import java.util.List;

/**
 * すべてを public にした注文です。中で使うだけの手順まで、外から呼べます。
 * 正しく使うには「addItem のあとに recalculate を呼ぶ」などの決まりを、使う側が知っている必要があります。
 */
public class OpenOrder {
    // 1行あたりの数量の上限
    public static final int MAX_QUANTITY = 99;
    // 顧客IDを持つフィールド
    private final String customerId;
    // 行の一覧を持つフィールド（中身を外から変えられる List）
    private final List<OpenOrderLine> lines;
    // 状態を文字列で持つフィールド（誰でも書き換えられる）
    private String status;
    // 合計金額を持つフィールド（recalculate を呼ぶまで古いまま）
    private int total;

    /**
     * 注文を作ります。誰でも、どんな状態の注文でも作れます。
     *
     * @param customerId 顧客ID
     * @param lines      行の一覧（そのまま持つ）
     * @param status     状態（"DRAFT" または "PLACED"）
     */
    public OpenOrder(String customerId, List<OpenOrderLine> lines, String status) {
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の一覧を、コピーせずにそのまま保存する
        this.lines = lines;
        // 引数の状態を、確認せずにフィールドへ保存する
        this.status = status;
    }

    /**
     * 商品を足します。数量の上限は、ここでだけ確認します。合計は更新しません。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public void addItem(String productId, int unitPrice, int quantity) {
        // 数量が上限を超えるとき
        if (quantity > MAX_QUANTITY) {
            // 足さずに拒否する
            throw new IllegalArgumentException("数量が上限を超えています: " + quantity);
        }
        // 行を作って、一覧に足す
        lines.add(new OpenOrderLine(productId, unitPrice, quantity));
    }

    /**
     * 合計を計算し直します。中で使うための手順ですが、public なので外から呼ぶ決まりになっています。
     */
    public void recalculate() {
        // 合計を0から数え直す
        int sum = 0;
        // 行を1つずつ見る
        for (OpenOrderLine line : lines) {
            // 単価に数量を掛けて、合計に足す
            sum += line.unitPrice() * line.quantity();
        }
        // 数え直した合計を、フィールドに保存する
        this.total = sum;
    }

    /**
     * 状態を書き換えます。行が空でも、誰でも「確定済み」にできます。
     *
     * @param status 新しい状態
     */
    public void setStatus(String status) {
        // 確認せずに、状態を書き換える
        this.status = status;
    }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 行の一覧（中身を外から変えられる） */
    public List<OpenOrderLine> lines() { return lines; }

    /** @return 状態 */
    public String status() { return status; }

    /** @return 合計金額（recalculate を呼ぶまで古いまま） */
    public int total() { return total; }

    /**
     * 空の下書きの注文を作る補助メソッドです。
     *
     * @param customerId 顧客ID
     * @return 空の下書きの注文
     */
    public static OpenOrder draft(String customerId) {
        // 空の作業用の一覧と、下書きの状態で作って返す
        return new OpenOrder(customerId, new ArrayList<>(), "DRAFT");
    }
}
