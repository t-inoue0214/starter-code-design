package com.example.orders.ch20_visibility.order;

import java.util.ArrayList;
import java.util.List;

/**
 * 注文です。外に見せるのは「作る、足す、確定する、読む」の操作だけです。
 * 合計の計算や、同じ商品をまとめる手順は private にして、使う側が知らなくて済むようにしています。
 */
public final class Order {
    // 顧客IDを持つフィールド
    private final String customerId;
    // 行の一覧を持つフィールド（外には渡さない作業用の List）
    private final List<OrderLine> lines = new ArrayList<>();
    // 状態を持つフィールド（変えられるのは place だけ）
    private OrderStatus status = OrderStatus.DRAFT;

    /**
     * 注文を作ります。外からは create を使います。
     *
     * @param customerId 顧客ID
     */
    private Order(String customerId) {
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
    }

    /**
     * 空の下書きの注文を作ります。
     *
     * @param customerId 顧客ID
     * @return 空の下書きの注文
     */
    public static Order create(String customerId) {
        // ★重要★ 作り方を1つに絞る。外から、確定済みの注文や行の入った注文を直接作れない
        return new Order(customerId);
    }

    /**
     * 商品を足します。同じ商品がすでにあれば、数量をまとめます。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public void addItem(String productId, int unitPrice, int quantity) {
        // 確定済みのとき
        if (status != OrderStatus.DRAFT) {
            // 確定済みの注文には足せないので拒否する
            throw new IllegalStateException("確定済みの注文には、商品を足せません");
        }
        // 同じ商品の行があるか探す
        int index = indexOf(productId);
        // 同じ商品の行がないとき
        if (index < 0) {
            // 新しい行を作って足す（上限の確認は、行のコンストラクタが行う）
            lines.add(new OrderLine(productId, unitPrice, quantity));
            // ここで終わる
            return;
        }
        // 同じ商品の行を、数量をまとめた新しい行に置き換える
        lines.set(index, lines.get(index).plus(quantity));
    }

    /**
     * 注文を確定します。行が1つもなければ拒否します。
     */
    public void place() {
        // 行が1つもないとき
        if (lines.isEmpty()) {
            // 空の注文は確定できないので拒否する
            throw new IllegalStateException("行のない注文は確定できません");
        }
        // ★重要★ 状態を変えられるのは、確認を通ったこの場所だけ。外にセッターを見せない
        this.status = OrderStatus.PLACED;
    }

    /** @return 合計金額（円）。読むたびに計算するので、古くならない */
    public int total() {
        // 合計を0から数える
        int sum = 0;
        // 行を1つずつ見る
        for (OrderLine line : lines) {
            // 行の小計を、合計に足す
            sum += line.subtotal();
        }
        // 数えた合計を返す
        return sum;
    }

    /**
     * 同じ商品の行が、一覧の何番目にあるかを探します。中の手順なので private にしています。
     *
     * @param productId 探す商品ID
     * @return 見つかった位置。なければ -1
     */
    private int indexOf(String productId) {
        // 一覧を先頭から見る
        for (int i = 0; i < lines.size(); i++) {
            // 商品IDが同じとき
            if (lines.get(i).productId().equals(productId)) {
                // その位置を返す
                return i;
            }
        }
        // 見つからなかったことを返す
        return -1;
    }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 状態 */
    public OrderStatus status() { return status; }

    /** @return 行の一覧（変更できないコピー） */
    public List<OrderLine> lines() {
        // 作業用の List は渡さず、変更できないコピーを返す
        return List.copyOf(lines);
    }
}
