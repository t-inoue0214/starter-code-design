package com.example.orders.ch17_class_kinds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * クラスの種類（DTO、Value Object、Entity など）の違いを、動かして確かめるテストです。
 * このテストは、最初から全て green です。違いを読み取るための見本として読んでください。
 */
class ClassKindsTest {
    /** DTO は値を運ぶだけで、値の検証をしない。 */
    @Test
    @DisplayName("DTO は、受け取った値をそのまま運ぶだけで、不正な値でも拒否しない")
    void dtoCarriesAnyValue() {
        // 負の請求額でも、DTO は拒否せずに作れる
        OrderSummaryDto dto = new OrderSummaryDto("O1", "C1", -100);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("O1", dto.orderId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(-100, dto.total());
    }

    /** Value Object は、値が同じなら等しく、不正な値は作れない。 */
    @Test
    @DisplayName("Value Object は、値が同じなら等しく、不正な値は作れない")
    void valueObjectIsEqualByValueAndAlwaysValid() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(EmailAddress.of("Taro@Example.com"), EmailAddress.of(" taro@example.com "));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("example.com", EmailAddress.of("taro@example.com").domain());
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> EmailAddress.of("not-an-address"));
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> EmailAddress.of(null));
    }

    /** Entity は、IDが同じなら同じもの。状態が変わっても同じもの。 */
    @Test
    @DisplayName("Entity は、IDが同じなら同じもので、状態（数量）が変わっても同じもの")
    void entityIsEqualById() {
        // P1の在庫を、10個で作る
        InventoryItem a = new InventoryItem("P1", 10);
        // 同じ商品IDで、数量だけが違う在庫を作る
        InventoryItem b = new InventoryItem("P1", 3);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(a, b);
        // 一方の在庫から、4個を引き当てる
        a.reserve(4);
        // 在庫数が 6 になることを確かめる
        assertEquals(6, a.quantity());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(a, b);
        // 実際の値が、期待値と異なることを確かめる
        assertNotEquals(a, new InventoryItem("P2", 6));
    }

    /** Entity は自分のルールを守る。足りなければ状態を変えない。 */
    @Test
    @DisplayName("Entity は自分のルールを持ち、足りないときは状態を変えない")
    void entityKeepsItsRule() {
        // P1の在庫を、2個で作る
        InventoryItem item = new InventoryItem("P1", 2);
        // 例外が投げられることを確かめる
        assertThrows(OutOfStockException.class, () -> item.reserve(3));
        // 在庫数が 2 になることを確かめる
        assertEquals(2, item.quantity());
    }

    /** Enum は取りうる値が決まっていて、値ごとの振る舞いを持てる。 */
    @Test
    @DisplayName("Enum は、取りうる値が決まっていて、値ごとの振る舞いを持てる")
    void enumHasFixedValuesWithBehavior() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(3, OrderStatus.values().length);
        // 条件が成り立つことを確かめる
        assertTrue(OrderStatus.PLACED.canCancel());
        // 条件が成り立たないことを確かめる
        assertFalse(OrderStatus.SHIPPED.canCancel());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("出荷済み", OrderStatus.SHIPPED.label());
    }

    /** Enum は、DB の数値や文字列と、相互に変換できる。 */
    @Test
    @DisplayName("Enum は、DB の数値のコードや文字列の名前と、相互に変換できる")
    void enumConvertsToAndFromDbValues() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(OrderStatus.PLACED, OrderStatus.fromCode(1));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(9, OrderStatus.CANCELLED.code());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromCode(9));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("SHIPPED", OrderStatus.SHIPPED.name());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromName(" shipped "));
        // すべての状態で、往復の変換を確かめる
        for (OrderStatus status : OrderStatus.values()) {
            // 実際の値が、期待値と等しいことを確かめる
            assertEquals(status, OrderStatus.fromCode(status.code()));
            // 実際の値が、期待値と等しいことを確かめる
            assertEquals(status, OrderStatus.fromName(status.name()));
        }
    }

    /** 未知の値は、黙って別の値にせず、例外で知らせる。 */
    @Test
    @DisplayName("Enum は、未知のコードや名前を、黙って受け入れずに例外で知らせる")
    void enumRejectsUnknownDbValues() {
        // 例外が投げられることを確かめる
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromCode(99));
        // 例外が投げられることを確かめる
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromName("UNKNOWN"));
        // 例外が投げられることを確かめる
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.fromName(null));
    }

    /** DB には、ordinal（宣言順）ではなく、専用のコードを保存する。 */
    @Test
    @DisplayName("DB に保存するコードは、宣言順（ordinal）とは別の値にしておく")
    void codeIsIndependentOfDeclarationOrder() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(2, OrderStatus.CANCELLED.ordinal());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(9, OrderStatus.CANCELLED.code());
    }

    /** イベントは、起きた事実と、イベントID・発生時刻・要求元を持つ。 */
    @Test
    @DisplayName("イベントは、起きた事実に加えて、イベントID・発生時刻・要求元を持つ")
    void eventHoldsAFactWithIdAndOrigin() {
        // ★重要★ 時刻を固定した時計を作る（テストで発生時刻を決め打ちできる）
        Clock clock = Clock.fixed(Instant.parse("2026-05-10T01:00:00Z"), ZoneOffset.UTC);
        // ★重要★ IDの生成方法と時計を渡して、イベントを新しく作る
        OrderShippedEvent event = OrderShippedEvent.occur("O1", LocalDate.of(2026, 5, 10), "warehouse-system",
                () -> "EVT-1", clock);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("EVT-1", event.eventId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(Instant.parse("2026-05-10T01:00:00Z"), event.occurredAt());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("warehouse-system", event.requestedBy());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("O1", event.orderId());
    }

    /** 標準の作り方では、毎回違うイベントIDが付く。 */
    @Test
    @DisplayName("標準の作り方では、同じ内容でも、毎回違うイベントIDが付く")
    void eachNewEventGetsItsOwnId() {
        // 標準の方法で、1つ目のイベントを作る
        OrderShippedEvent a = OrderShippedEvent.occur("O1", LocalDate.of(2026, 5, 10), "batch");
        // 同じ内容で、2つ目のイベントを作る
        OrderShippedEvent b = OrderShippedEvent.occur("O1", LocalDate.of(2026, 5, 10), "batch");
        // 実際の値が、期待値と異なることを確かめる
        assertNotEquals(a.eventId(), b.eventId());
        // 実際の値が、期待値と異なることを確かめる
        assertNotEquals(a, b);
    }

    /** 復元したイベントは、保存されていたIDのまま。同じIDなら同じイベント。 */
    @Test
    @DisplayName("復元したイベントは、保存されていたイベントIDのままで、同じIDなら同じイベントとみなす")
    void restoredEventKeepsItsId() {
        // 保存されていた発生時刻を用意する
        Instant at = Instant.parse("2026-05-10T01:00:00Z");
        // 保存されていたイベントを復元する
        OrderShippedEvent saved = OrderShippedEvent.restore("EVT-9", at, "batch", "O1", LocalDate.of(2026, 5, 10));
        // 同じイベントIDで、もう一度復元する
        OrderShippedEvent again = OrderShippedEvent.restore("EVT-9", at, "batch", "O1", LocalDate.of(2026, 5, 10));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("EVT-9", saved.eventId());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(saved, again);
    }

    /** Result は成功と失敗を戻り値で返す。失敗から値を取り出すと例外。 */
    @Test
    @DisplayName("Result は、成功と失敗を戻り値で表し、失敗から値は取り出せない")
    void resultRepresentsSuccessOrFailure() {
        // 成功の結果を作る
        Result<Integer> ok = Result.ok(100);
        // 失敗の結果を作る
        Result<Integer> ng = Result.failure("在庫不足");
        // 条件が成り立つことを確かめる
        assertTrue(ok.isOk());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(100, ok.value());
        // 条件が成り立たないことを確かめる
        assertFalse(ng.isOk());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("在庫不足", ng.error());
        // 例外が投げられることを確かめる
        assertThrows(IllegalStateException.class, ng::value);
    }

    /** Config は、指定がない項目は標準値になり、範囲外の値は拒否する。 */
    @Test
    @DisplayName("Config は、指定のない項目に標準値を使い、範囲外の設定値は拒否する")
    void configFallsBackToDefaults() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(500_000, ShopConfig.defaults().paymentLimit());
        // 設定値を入れるMapを作る
        Map<String, String> props = new HashMap<>();
        // 決済上限だけを、100万円に指定する
        props.put("payment.limit", "1000000");
        // 指定したMapから、設定を作る
        ShopConfig config = ShopConfig.fromMap(props);
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(1_000_000, config.paymentLimit());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(3, config.seasonalRatePercent());
        // 季節割引の割合に、範囲外の150%を指定する
        props.put("seasonal.rate", "150");
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> ShopConfig.fromMap(props));
    }

    /** バリデータは、エラーコードの一覧として、すべてのエラーを集めて返す。 */
    @Test
    @DisplayName("バリデータは、最初のエラーで止めず、エラーコードの一覧としてすべて返す")
    void validatorCollectsAllErrorsAsCodes() {
        // バリデータを作る
        OrderInputValidator validator = new OrderInputValidator();
        // 条件が成り立つことを確かめる
        assertTrue(validator.validate("O1", "C1", 1).isEmpty());
        // 3つの項目がすべて不正な入力を検査する
        List<ValidationError> errors = validator.validate("", null, 0);
        // 件数が 3 になることを確かめる
        assertEquals(3, errors.size());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("E001", errors.get(0).code());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(ValidationError.QUANTITY_OUT_OF_RANGE, errors.get(2));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("数量は1以上99以下にしてください", errors.get(2).message());
    }

    /** Util は状態を持たず、クラス名から直接呼ぶ。 */
    @Test
    @DisplayName("Util は、状態を持たない静的メソッドで、クラス名から直接呼ぶ")
    void utilIsStatelessAndStatic() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("1,234,567円", YenFormat.format(1_234_567));
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("0円", YenFormat.format(0));
    }

    /** 定数クラスは、名前の付いた変わらない値を提供する。 */
    @Test
    @DisplayName("定数クラスは、名前の付いた上限値を提供し、バリデータがそれを使う")
    void constantsGiveNamesToLimits() {
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(99, OrderLimits.MAX_QUANTITY_PER_LINE);
        // バリデータを作る
        OrderInputValidator validator = new OrderInputValidator();
        // 条件が成り立つことを確かめる
        assertTrue(validator.validate("O1", "C1", OrderLimits.MAX_QUANTITY_PER_LINE).isEmpty());
        // 条件が成り立たないことを確かめる
        assertFalse(validator.validate("O1", "C1", OrderLimits.MAX_QUANTITY_PER_LINE + 1).isEmpty());
    }

    /** ビルダーは、省略できる項目を省略して組み立て、必須項目がなければ作れない。 */
    @Test
    @DisplayName("ビルダーは、項目に名前を付けて組み立て、必須の宛先がなければ作れない")
    void builderAssemblesStepByStep() {
        // ビルダーで、項目に名前を付けながらメールを組み立てる
        MailMessage mail = MailMessage.builder()
                .to("taro@example.com")
                .subject("ご注文ありがとうございます")
                .cc("boss@example.com")
                .build();
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("taro@example.com", mail.to());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals("", mail.body());
        // 実際の値が、期待値と等しいことを確かめる
        assertEquals(List.of("boss@example.com"), mail.cc());
        // 例外が投げられることを確かめる
        assertThrows(OrderValidationException.class, () -> MailMessage.builder().subject("件名だけ").build());
    }
}
