package com.example.orders.ch23_naming;

/**
 * 名前を直した注文です。フィールドの名前に、意味と単位を含めています。
 */
public final class Order {
    // 取り消せる期限（確定してからの日数）
    static final int CANCELLABLE_DAYS = 7;
    // 状態を持つフィールド
    private final OrderStatus status;
    // ★重要★ 税抜きの小計であることを、名前で伝える。税込みの金額と取り違えない
    private final int subtotalBeforeTax;
    // ★重要★ 何からの日数かを、名前で伝える
    private final int daysSincePlaced;

    /**
     * 注文を作ります。
     *
     * @param status            状態
     * @param subtotalBeforeTax 税抜きの小計（円）
     * @param daysSincePlaced   確定してからの日数
     */
    public Order(OrderStatus status, int subtotalBeforeTax, int daysSincePlaced) {
        // 引数の状態を、フィールドに保存する
        this.status = status;
        // 引数の税抜きの小計を、フィールドに保存する
        this.subtotalBeforeTax = subtotalBeforeTax;
        // 引数の日数を、フィールドに保存する
        this.daysSincePlaced = daysSincePlaced;
    }

    /**
     * 取り消せるかを答えます。
     *
     * @return 確定済みで、期限内なら true
     */
    public boolean isCancellable() {
        // ★重要★ 名前が「何が真か」を決めている。if (order.isCancellable()) が、そのまま文として読める
        return status == OrderStatus.PLACED && daysSincePlaced <= CANCELLABLE_DAYS;
    }

    /** @return 状態 */
    public OrderStatus status() { return status; }

    /** @return 税抜きの小計（円） */
    public int subtotalBeforeTax() { return subtotalBeforeTax; }

    /** @return 確定してからの日数 */
    public int daysSincePlaced() { return daysSincePlaced; }
}
