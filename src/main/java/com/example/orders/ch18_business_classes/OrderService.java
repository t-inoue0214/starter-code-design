package com.example.orders.ch18_business_classes;

import com.example.orders.ch17_class_kinds.InventoryItem;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.PaymentDeclinedException;
import java.util.List;
import java.util.Map;

/**
 * サービス（ユースケース）の例です。「注文する」「キャンセルする」の処理の流れだけを決めます。
 * ルールの中身（税率、上限、権限、在庫の足りる足りない）は、持っている部品に任せます。
 */
public class OrderService {
    // 注文（集約）の保存先を持つフィールド
    private final OrderRepository orders;
    // 在庫の保存先を持つフィールド
    private final ItemRepository items;
    // 決済の窓口を持つフィールド
    private final PaymentGateway gateway;
    // 決済額の上限を判定する部品を持つフィールド
    private final PaymentLimitPolicy limitPolicy;
    // キャンセルの権限を判定する部品を持つフィールド
    private final CancelAuthorizationPolicy authorizationPolicy;
    // 税額を計算する部品を持つフィールド
    private final TaxCalculator taxCalculator;
    // 在庫の引当を行う部品を持つフィールド
    private final StockAllocationService allocationService;
    // 集約を組み立てる部品を持つフィールド
    private final PlacedOrderFactory factory = new PlacedOrderFactory();

    /**
     * サービスを作ります。必要な部品は、すべて外から受け取ります。
     *
     * @param orders              注文の保存先
     * @param items               在庫の保存先
     * @param gateway             決済の窓口
     * @param limitPolicy         決済額の上限のポリシー
     * @param authorizationPolicy キャンセルの認可ポリシー
     * @param taxCalculator       税額の計算クラス
     * @param allocationService   在庫の引当を行うドメインサービス
     */
    public OrderService(OrderRepository orders, ItemRepository items, PaymentGateway gateway,
                        PaymentLimitPolicy limitPolicy, CancelAuthorizationPolicy authorizationPolicy,
                        TaxCalculator taxCalculator, StockAllocationService allocationService) {
        // 引数の注文の保存先を、フィールドに保存する
        this.orders = orders;
        // 引数の在庫の保存先を、フィールドに保存する
        this.items = items;
        // 引数の決済の窓口を、フィールドに保存する
        this.gateway = gateway;
        // 引数の上限ポリシーを、フィールドに保存する
        this.limitPolicy = limitPolicy;
        // 引数の認可ポリシーを、フィールドに保存する
        this.authorizationPolicy = authorizationPolicy;
        // 引数の税の計算クラスを、フィールドに保存する
        this.taxCalculator = taxCalculator;
        // 引数のドメインサービスを、フィールドに保存する
        this.allocationService = allocationService;
    }

    /**
     * 注文を受け付けます。
     *
     * @param input 入力の注文
     * @return 請求額（税込、円）
     */
    public int place(Order input) {
        // 入力から、集約を組み立てる（ファクトリの仕事）
        PlacedOrder order = factory.create(input);
        // 小計に税額を足して、請求額を求める（計算クラスの仕事）
        int total = order.subtotal() + taxCalculator.taxOf(order.subtotal(), order.orderedOn());
        // 決済額が上限以内かを判定する（ポリシーの仕事）
        PolicyDecision decision = limitPolicy.check(total);
        // 上限を超えているとき
        if (!decision.allowed()) {
            // 判定の理由を添えて、決済の拒否として扱う
            throw new PaymentDeclinedException(decision.reason());
        }
        // 引き当てる在庫を、保存先から取り出す（リポジトリの仕事）
        Map<String, InventoryItem> stock = items.findByIds(productIdsOf(input));
        // ★重要★ 引当のルールは、ドメインサービスに任せる。ここは呼ぶだけ
        allocationService.allocate(order, stock);
        // 外部の決済サービスへ請求する（ゲートウェイの仕事）。拒否されたら、ここで例外になる
        gateway.charge(order.customerId(), total);
        // ★重要★ 保存は最後に行う。決済が拒否されたときは、在庫も注文も保存されない
        stock.values().forEach(items::save);
        // 注文を、明細ごと保存する（集約の単位で保存する）
        orders.save(order);
        // 請求額を返す
        return total;
    }

    /**
     * 注文をキャンセルします。
     *
     * @param orderId     キャンセルする注文のID
     * @param requesterId 操作する人のID
     * @param role        操作する人の役割
     */
    public void cancel(String orderId, String requesterId, Role role) {
        // 注文を、明細ごと取り出す。なければ拒否する
        PlacedOrder order = orders.find(orderId)
                .orElseThrow(() -> new OrderValidationException("注文が見つかりません: " + orderId));
        // 権限があるかを判定する（認可ポリシーの仕事）
        PolicyDecision decision = authorizationPolicy.check(order, requesterId, role);
        // 権限がないとき
        if (!decision.allowed()) {
            // 判定の理由を添えて、拒否する
            throw new OrderValidationException(decision.reason());
        }
        // ★重要★ キャンセルできる状態かどうかは、注文（集約）が自分で判断する
        order.cancel();
        // キャンセル後の注文を、保存する
        orders.save(order);
    }

    /**
     * 入力の注文から、商品IDの一覧を取り出します。
     *
     * @param input 入力の注文
     * @return 商品IDの一覧
     */
    private List<String> productIdsOf(Order input) {
        // 明細から商品IDだけを集めて返す
        return input.lines().stream().map(OrderLine::productId).toList();
    }
}
