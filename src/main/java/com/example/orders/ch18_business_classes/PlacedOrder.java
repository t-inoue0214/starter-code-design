package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.OrderLimits;
import com.example.orders.ch17_class_kinds.OrderStatus;
import com.example.orders.domain.OrderValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 集約（Aggregate）のルートです。注文と明細を「ひとまとまり」として扱います。
 * 明細は、必ずこのクラスを通して変更します。これにより、「明細は20行まで」のような
 * 注文全体にかかるルールを、1か所で守れます。
 */
public class PlacedOrder {
    // 注文IDを持つフィールド
    private final String id;
    // 注文した顧客のIDを持つフィールド
    private final String customerId;
    // 注文日を持つフィールド
    private final LocalDate orderedOn;
    // 注文の状態を持つフィールド
    private OrderStatus status;
    // 明細の一覧を持つフィールド。外には出さず、このクラスだけが変更する
    private final List<PlacedLine> lines = new ArrayList<>();

    /**
     * 注文を作ります。作れるのは、同じパッケージのファクトリだけです。
     *
     * @param id         注文ID
     * @param customerId 顧客ID
     * @param orderedOn  注文日
     * @param status     注文の状態
     */
    PlacedOrder(String id, String customerId, LocalDate orderedOn, OrderStatus status) {
        // 引数の注文IDを、フィールドに保存する
        this.id = id;
        // 引数の顧客IDを、フィールドに保存する
        this.customerId = customerId;
        // 引数の注文日を、フィールドに保存する
        this.orderedOn = orderedOn;
        // 引数の状態を、フィールドに保存する
        this.status = status;
    }

    /**
     * 明細を足します。同じ商品があれば、数量をまとめます。
     *
     * @param productId 商品ID
     * @param unitPrice 単価（円）
     * @param quantity  数量
     */
    public void addLine(String productId, int unitPrice, int quantity) {
        // 商品IDが空のとき
        if (productId == null || productId.isBlank()) {
            // 不正な入力として拒否する
            throw new OrderValidationException("商品IDは必須です");
        }
        // 単価が負のとき
        if (unitPrice < 0) {
            // 不正な入力として拒否する
            throw new OrderValidationException("単価は0円以上にしてください: " + unitPrice);
        }
        // 同じ商品の明細が、すでにあるかを探す
        int index = indexOf(productId);
        // すでにある明細の数量を、足したあとの数量として用意する
        int merged = index >= 0 ? lines.get(index).quantity() + quantity : quantity;
        // 数量が1未満か、1明細の上限を超えるとき
        if (quantity < 1 || merged > OrderLimits.MAX_QUANTITY_PER_LINE) {
            // 不正な入力として拒否する
            throw new OrderValidationException("数量が範囲外です: " + productId);
        }
        // 同じ商品がすでにあるとき
        if (index >= 0) {
            // 数量をまとめた明細に、差し替える
            lines.set(index, lines.get(index).plusQuantity(quantity));
            // 新しい明細は足さないので、ここで終わる
            return;
        }
        // 明細の数が、注文の上限に達しているとき
        if (lines.size() >= OrderLimits.MAX_LINES_PER_ORDER) {
            // ★重要★ 「注文全体で20行まで」は、明細1つでは分からない。だから集約のルートが守る
            throw new OrderValidationException("明細が多すぎます: " + id);
        }
        // 新しい明細を足す
        lines.add(new PlacedLine(productId, unitPrice, quantity));
    }

    /**
     * 注文をキャンセルします。キャンセルできない状態のときは、拒否します。
     */
    public void cancel() {
        // 今の状態が、キャンセルできない状態のとき
        if (!status.canCancel()) {
            // ★重要★ 状態に関するルールは、状態を持つ注文自身が守る
            throw new OrderValidationException("この状態ではキャンセルできません: " + status.label());
        }
        // 状態をキャンセル済みに変える
        status = OrderStatus.CANCELLED;
    }

    /**
     * 明細を、状態を復元するために足します。検証はせず、保存されていた内容をそのまま戻します。
     *
     * @param line 保存されていた明細
     */
    void restoreLine(PlacedLine line) {
        // 保存されていた明細を、そのまま足す
        lines.add(line);
    }

    /**
     * 商品IDが同じ明細の位置を探します。
     *
     * @param productId 商品ID
     * @return 見つかった位置。なければ -1
     */
    private int indexOf(String productId) {
        // 先頭から順に、明細を調べる
        for (int i = 0; i < lines.size(); i++) {
            // 商品IDが一致したとき
            if (lines.get(i).productId().equals(productId)) {
                // 見つかった位置を返す
                return i;
            }
        }
        // 見つからなかったので、-1 を返す
        return -1;
    }

    /** @return 全明細の金額の合計（税抜） */
    public int subtotal() {
        // 明細の金額を、全部足して返す
        return lines.stream().mapToInt(PlacedLine::subtotal).sum();
    }

    /** @return 明細の一覧。書き換えられないコピー */
    public List<PlacedLine> lines() {
        // ★重要★ 内部の一覧は渡さず、コピーを渡す。外から勝手に変更させない
        return List.copyOf(lines);
    }

    /** @return 注文ID */
    public String id() { return id; }

    /** @return 顧客ID */
    public String customerId() { return customerId; }

    /** @return 注文日 */
    public LocalDate orderedOn() { return orderedOn; }

    /** @return 注文の状態 */
    public OrderStatus status() { return status; }
}
