package com.example.orders.ch18_business_classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch17_class_kinds.InventoryItem;
import com.example.orders.ch17_class_kinds.OrderStatus;
import com.example.orders.ch18_business_classes.before.AllInOneOrderService;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 業務ロジックを担うクラスの種類（サービス、集約、ポリシーなど）の違いを、動かして確かめるテストです。
 * このテストは、最初から全て green です。違いを読み取るための見本として読んでください。
 */
class BusinessClassesTest {
    // 税率が変わったあとの注文日（税率10%）
    private static final LocalDate AFTER = LocalDate.of(2024, 4, 1);

    /**
     * 注文を作る補助メソッドです。
     *
     * @param id    注文ID
     * @param date  注文日
     * @param lines 明細
     * @return 作った注文
     */
    private static Order order(String id, LocalDate date, OrderLine... lines) {
        // C1 という顧客で、クーポンなしの注文を作って返す
        return new Order(id, new Customer("C1", "太郎", MemberRank.REGULAR, null, null),
                List.of(lines), null, date);
    }

    /**
     * 組み立て済みのサービスを作る補助メソッドです。
     *
     * @param orders  注文の保存先
     * @param items   在庫の保存先
     * @param gateway 決済の窓口
     * @return 作ったサービス（決済額の上限は 300000 円）
     */
    private static OrderService service(OrderRepository orders, ItemRepository items, PaymentGateway gateway) {
        // 部品を渡して、サービスを組み立てて返す
        return new OrderService(orders, items, gateway, new PaymentLimitPolicy(300000),
                new CancelAuthorizationPolicy(), new TaxCalculator(), new StockAllocationService());
    }

    /** 計算クラスは、入力だけで結果が決まる。 */
    @Test
    @DisplayName("計算クラスは、日付に応じた税率で、入力だけから税額を計算する")
    void calculatorDependsOnlyOnInput() {
        // 税の計算クラスを作る
        TaxCalculator calculator = new TaxCalculator();
        // 税率変更前は、8%で計算される
        assertEquals(80, calculator.taxOf(1000, LocalDate.of(2019, 9, 30)));
        // 税率変更後は、10%で計算される
        assertEquals(100, calculator.taxOf(1000, LocalDate.of(2019, 10, 1)));
        // 円未満は、切り捨てられる
        assertEquals(10, calculator.taxOf(105, AFTER));
    }

    /** ポリシーは判定だけをして、理由を値で返す。 */
    @Test
    @DisplayName("ポリシーは、可否と理由を値で返し、例外も状態変更もしない")
    void policyOnlyDecides() {
        // 上限が 1000 円のポリシーを作る
        PaymentLimitPolicy policy = new PaymentLimitPolicy(1000);
        // 上限ちょうどは、許可される
        assertTrue(policy.check(1000).allowed());
        // 上限を超えると、許可されない
        assertFalse(policy.check(1001).allowed());
        // 許可されないときは、理由が入っている
        assertTrue(policy.check(1001).reason().contains("1001"));
        // 顧客ごとに違う上限は、別の値で同じクラスを作れば表せる
        assertTrue(new PaymentLimitPolicy(5000).check(1001).allowed());
    }

    /** 認可ポリシーは、誰がやってよいかだけを判定する。 */
    @Test
    @DisplayName("認可ポリシーは、運用担当者と本人だけを許可する")
    void authorizationPolicyChecksWhoCanDo() {
        // C1 の注文を作る
        PlacedOrder placed = new PlacedOrderFactory()
                .create(order("O1", AFTER, new OrderLine("P1", 100, 1)));
        // 認可ポリシーを作る
        CancelAuthorizationPolicy policy = new CancelAuthorizationPolicy();
        // 運用担当者は、許可される
        assertTrue(policy.check(placed, "X", Role.ADMIN).allowed());
        // 本人は、許可される
        assertTrue(policy.check(placed, "C1", Role.CUSTOMER).allowed());
        // 他人は、許可されない
        assertFalse(policy.check(placed, "C2", Role.CUSTOMER).allowed());
    }

    /** 仕様は、1条件1クラスで、組み合わせて使う。 */
    @Test
    @DisplayName("仕様は、1条件を1クラスにして、and・or・not で組み合わせられる")
    void specificationsCombine() {
        // 小計 6000 円で、商品 P1 を含む注文を作る
        PlacedOrder placed = new PlacedOrderFactory()
                .create(order("O1", AFTER, new OrderLine("P1", 3000, 2)));
        // 5000 円以上という条件を作る
        OrderSpecification big = new MinimumAmountSpecification(5000);
        // 商品 GIFT を含むという条件を作る
        OrderSpecification gift = new HasProductSpecification("GIFT");
        // 5000 円以上である
        assertTrue(big.isSatisfiedBy(placed));
        // GIFT は含まない
        assertFalse(gift.isSatisfiedBy(placed));
        // 両方を満たす、という組み合わせは満たさない
        assertFalse(big.and(gift).isSatisfiedBy(placed));
        // どちらかを満たす、という組み合わせは満たす
        assertTrue(big.or(gift).isSatisfiedBy(placed));
        // 5000 円以上ではない、という組み合わせは満たさない
        assertFalse(big.not().isSatisfiedBy(placed));
    }

    /** 集約のルートは、明細をまとめて、全体のルールを守る。 */
    @Test
    @DisplayName("集約は、同じ商品の明細をまとめ、注文全体のルール（行数の上限）を守る")
    void aggregateProtectsInvariants() {
        // 空の注文を作る
        PlacedOrder placed = new PlacedOrder("O1", "C1", AFTER, OrderStatus.PLACED);
        // 同じ商品を2回足す
        placed.addLine("P1", 100, 2);
        // 同じ商品を、もう1回足す
        placed.addLine("P1", 100, 3);
        // 明細は1行にまとまる
        assertEquals(1, placed.lines().size());
        // 数量は合計される
        assertEquals(5, placed.lines().get(0).quantity());
        // 合計金額は、明細から計算される
        assertEquals(500, placed.subtotal());
        // 1明細の数量の上限を超える追加は、拒否される
        assertThrows(OrderValidationException.class, () -> placed.addLine("P1", 100, 95));
        // 数量 0 は、拒否される
        assertThrows(OrderValidationException.class, () -> placed.addLine("P2", 100, 0));
        // 違う商品を、行数の上限まで足す
        for (int i = 2; i <= 20; i++) {
            // 商品 P2 から P20 までを足す
            placed.addLine("P" + i, 100, 1);
        }
        // 21 行目は、拒否される
        assertThrows(OrderValidationException.class, () -> placed.addLine("P21", 100, 1));
    }

    /** 集約の内部は、外から書き換えられない。 */
    @Test
    @DisplayName("集約の明細は、外から直接は書き換えられない")
    void aggregateHidesItsLines() {
        // 1明細の注文を作る
        PlacedOrder placed = new PlacedOrderFactory()
                .create(order("O1", AFTER, new OrderLine("P1", 100, 1)));
        // 返された一覧を直接変更しようとすると、失敗する
        assertThrows(UnsupportedOperationException.class, () -> placed.lines().clear());
    }

    /** 状態のルールは、状態を持つ集約自身が守る。 */
    @Test
    @DisplayName("集約は、キャンセルできない状態のキャンセルを拒否する")
    void aggregateOwnsStateRules() {
        // 出荷済みの注文を復元する
        PlacedOrder shipped = new PlacedOrderFactory()
                .restore("O1", "C1", AFTER, OrderStatus.SHIPPED, List.of());
        // 出荷済みはキャンセルできない
        assertThrows(OrderValidationException.class, shipped::cancel);
        // 注文済みの注文を作る
        PlacedOrder placed = new PlacedOrder("O2", "C1", AFTER, OrderStatus.PLACED);
        // キャンセルする
        placed.cancel();
        // キャンセル済みになる
        assertEquals(OrderStatus.CANCELLED, placed.status());
    }

    /** ファクトリは、新規作成と復元を分ける。 */
    @Test
    @DisplayName("ファクトリは、新規作成では検証し、復元では保存内容をそのまま戻す")
    void factorySeparatesCreateAndRestore() {
        // ファクトリを作る
        PlacedOrderFactory factory = new PlacedOrderFactory();
        // 明細のない入力は、新規作成では拒否される
        assertThrows(OrderValidationException.class, () -> factory.create(order("O1", AFTER)));
        // 新規作成の状態は、注文済み
        assertEquals(OrderStatus.PLACED,
                factory.create(order("O2", AFTER, new OrderLine("P1", 100, 1))).status());
        // 復元では、保存されていた状態がそのまま戻る
        assertEquals(OrderStatus.CANCELLED,
                factory.restore("O3", "C1", AFTER, OrderStatus.CANCELLED, List.of()).status());
    }

    /** ドメインサービスは、複数の在庫にまたがり、全部か無しかで引き当てる。 */
    @Test
    @DisplayName("ドメインサービスは、1つでも足りなければ、どの在庫も変更しない")
    void domainServiceAllocatesAllOrNothing() {
        // 在庫を2つ用意する（P2 は不足）
        Map<String, InventoryItem> items = new LinkedHashMap<>();
        // P1 の在庫は 10 個
        items.put("P1", new InventoryItem("P1", 10));
        // P2 の在庫は 1 個
        items.put("P2", new InventoryItem("P2", 1));
        // 2明細の注文を作る
        PlacedOrder placed = new PlacedOrderFactory().create(order("O1", AFTER,
                new OrderLine("P1", 100, 5), new OrderLine("P2", 100, 3)));
        // 引当は、在庫不足で失敗する
        assertThrows(OutOfStockException.class,
                () -> new StockAllocationService().allocate(placed, items));
        // 足りていた P1 も、減っていない
        assertEquals(10, items.get("P1").quantity());
    }

    /** リポジトリは、集約を明細ごと保存し、別のコピーとして取り出す。 */
    @Test
    @DisplayName("リポジトリは、集約を明細ごと保存し、取り出したコピーを変えても保存内容は変わらない")
    void repositoryStoresWholeAggregate() {
        // 注文のリポジトリを作る
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        // 2明細の注文を保存する
        repository.save(new PlacedOrderFactory().create(order("O1", AFTER,
                new OrderLine("P1", 100, 1), new OrderLine("P2", 200, 2))));
        // 取り出した注文を変更する（保存はしない）
        repository.find("O1").orElseThrow().cancel();
        // 保存された内容は、変わっていない
        assertEquals(OrderStatus.PLACED, repository.find("O1").orElseThrow().status());
        // 明細も一緒に取り出せる
        assertEquals(2, repository.find("O1").orElseThrow().lines().size());
        // 存在しない注文は、空になる
        assertTrue(repository.find("none").isEmpty());
    }

    /** サービスは、部品を呼ぶ順番だけを決める。 */
    @Test
    @DisplayName("サービスは、税込の請求額を決済し、在庫と注文を保存する")
    void serviceCoordinatesParts() {
        // 在庫を用意する
        InMemoryItemRepository items = new InMemoryItemRepository();
        // P1 の在庫は 10 個
        items.add("P1", 10);
        // 注文のリポジトリを作る
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        // 決済の窓口を作る
        FakePaymentGateway gateway = new FakePaymentGateway(false);
        // サービスを組み立てる
        OrderService service = service(orders, items, gateway);
        // 1000 円の商品を 2 個、注文する
        int total = service.place(order("O1", AFTER, new OrderLine("P1", 1000, 2)));
        // 請求額は、小計 2000 円に税 200 円を足した額
        assertEquals(2200, total);
        // 決済の窓口に、同じ額が請求されている
        assertEquals(List.of(2200), gateway.charged());
        // 在庫が減っている
        assertEquals(8, items.quantityOf("P1"));
        // 注文が保存されている
        assertTrue(orders.find("O1").isPresent());
    }

    /** 決済が拒否されたら、何も保存されない。 */
    @Test
    @DisplayName("決済が拒否されたら、在庫も注文も保存されない")
    void nothingIsSavedWhenPaymentIsDeclined() {
        // 在庫を用意する
        InMemoryItemRepository items = new InMemoryItemRepository();
        // P1 の在庫は 10 個
        items.add("P1", 10);
        // 注文のリポジトリを作る
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        // すべて拒否する決済の窓口でサービスを組み立てる
        OrderService service = service(orders, items, new FakePaymentGateway(true));
        // 注文すると、決済の拒否になる
        assertThrows(PaymentDeclinedException.class,
                () -> service.place(order("O1", AFTER, new OrderLine("P1", 1000, 2))));
        // 在庫は、減っていない
        assertEquals(10, items.quantityOf("P1"));
        // 注文は、保存されていない
        assertTrue(orders.find("O1").isEmpty());
    }

    /** キャンセルは、権限と状態の両方を通ったときだけ成功する。 */
    @Test
    @DisplayName("キャンセルは、本人か運用担当者だけが行え、他人は拒否される")
    void cancelRequiresAuthorization() {
        // 在庫を用意する
        InMemoryItemRepository items = new InMemoryItemRepository();
        // P1 の在庫は 10 個
        items.add("P1", 10);
        // 注文のリポジトリを作る
        InMemoryOrderRepository orders = new InMemoryOrderRepository();
        // サービスを組み立てる
        OrderService service = service(orders, items, new FakePaymentGateway(false));
        // 注文する
        service.place(order("O1", AFTER, new OrderLine("P1", 1000, 1)));
        // 他人のキャンセルは、拒否される
        assertThrows(OrderValidationException.class, () -> service.cancel("O1", "C2", Role.CUSTOMER));
        // 本人のキャンセルは、成功する
        service.cancel("O1", "C1", Role.CUSTOMER);
        // 状態がキャンセル済みで保存されている
        assertEquals(OrderStatus.CANCELLED, orders.find("O1").orElseThrow().status());
        // 2回目のキャンセルは、状態のルールで拒否される
        assertThrows(OrderValidationException.class, () -> service.cancel("O1", "C1", Role.ADMIN));
    }

    /** 分ける前と分けた後で、結果は同じ。 */
    @Test
    @DisplayName("分ける前の実装と、分けた後の実装は、同じ入力に同じ結果を返す")
    void splitVersionBehavesLikeAllInOne() {
        // 分ける前の実装を作る
        AllInOneOrderService before = new AllInOneOrderService(new FakePaymentGateway(false));
        // P1 の在庫は 10 個
        before.addStock("P1", 10);
        // 分けた後の在庫を作る
        InMemoryItemRepository items = new InMemoryItemRepository();
        // P1 の在庫は 10 個
        items.add("P1", 10);
        // 分けた後の実装を作る
        OrderService after = service(new InMemoryOrderRepository(), items, new FakePaymentGateway(false));
        // 同じ注文を用意する
        Order input = order("O1", AFTER, new OrderLine("P1", 1000, 3));
        // 請求額が同じ
        assertEquals(before.place(input), after.place(input));
        // 在庫の残りが同じ
        assertEquals(before.stockOf("P1"), items.quantityOf("P1"));
        // 在庫不足の注文は、どちらも同じ例外になる
        Order tooMany = order("O2", AFTER, new OrderLine("P1", 1000, 50));
        // 分ける前は、在庫不足
        assertThrows(OutOfStockException.class, () -> before.place(tooMany));
        // 分けた後も、在庫不足
        assertThrows(OutOfStockException.class, () -> after.place(tooMany));
        // 上限を超える注文は、どちらも決済の拒否になる
        Order expensive = order("O3", AFTER, new OrderLine("P1", 90000, 4));
        // 分ける前は、決済の拒否
        assertThrows(PaymentDeclinedException.class, () -> before.place(expensive));
        // 分けた後も、決済の拒否
        assertThrows(PaymentDeclinedException.class, () -> after.place(expensive));
        // 他人のキャンセルは、どちらも拒否される
        assertThrows(OrderValidationException.class, () -> before.cancel("O1", "C2", false));
        // 分けた後も、拒否される
        assertThrows(OrderValidationException.class, () -> after.cancel("O1", "C2", Role.CUSTOMER));
    }
}
