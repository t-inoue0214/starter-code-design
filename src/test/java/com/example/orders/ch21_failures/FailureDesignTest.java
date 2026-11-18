package com.example.orders.ch21_failures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch21_failures.before.SilentOrderService;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 失敗の設計を、動かして確かめるテストです。
 * before の「すべて -1 で返す実装」と、失敗を種類ごとに分けた実装を比べます。
 * このテストは、最初から全て green です。
 */
class FailureDesignTest {

    /**
     * 商品 P001（単価1000円、在庫5個）を登録した台帳を作る補助メソッドです。
     *
     * @return 台帳
     */
    private static StockBook stockBook() {
        // 台帳を作る
        StockBook book = new StockBook();
        // 商品を1つ登録する
        book.register("P001", 1000, 5);
        // 台帳を返す
        return book;
    }

    /**
     * 決まった振る舞いの決済で、サービスを作る補助メソッドです。
     *
     * @param book   商品の台帳
     * @param client 決済会社の偽物
     * @return サービス
     */
    private static OrderService service(StockBook book, FakePaymentClient client) {
        // ゲートウェイで包んで、サービスを作って返す
        return new OrderService(book, new PaymentGateway(client));
    }

    /** すべて -1 だと、理由が区別できない。 */
    @Test
    @DisplayName("before は、入力の誤り、在庫不足、カード拒否、通信の失敗、プログラムの誤りを、すべて -1 で返す")
    void silentServiceHidesEveryReason() {
        // 承認する決済で、before の注文処理を作る
        SilentOrderService ok = new SilentOrderService(new FakePaymentClient(FakePaymentClient.Mode.APPROVE));
        // 商品を登録する
        ok.register("P001", 1000, 5);
        // 数量が数字でない入力は、-1
        assertEquals(-1, ok.place("C001", "P001", "abc"));
        // 在庫が足りない注文も、-1
        assertEquals(-1, ok.place("C001", "P001", "6"));
        // 顧客IDが null（呼び出し側のプログラムの誤り）も、-1 で隠れる
        assertEquals(-1, ok.place(null, "P001", "1"));
        // 断る決済で、before の注文処理を作る
        SilentOrderService declined = new SilentOrderService(new FakePaymentClient(FakePaymentClient.Mode.DECLINE));
        // 商品を登録する
        declined.register("P001", 1000, 5);
        // カードの拒否も、-1
        assertEquals(-1, declined.place("C001", "P001", "1"));
        // 通信に失敗する決済で、before の注文処理を作る
        SilentOrderService down = new SilentOrderService(new FakePaymentClient(FakePaymentClient.Mode.DOWN));
        // 商品を登録する
        down.register("P001", 1000, 5);
        // 通信の失敗も、-1
        assertEquals(-1, down.place("C001", "P001", "1"));
    }

    /** 入口のバリデータは、誤りをすべて集める。 */
    @Test
    @DisplayName("入口のバリデータは、最初の誤りで止めず、すべての誤りを集める")
    void validatorCollectsAllErrors() {
        // 顧客IDが空、数量が数字でない入力を検査する
        List<FieldError> errors = new OrderRequestValidator().validate(new OrderRequest(" ", "P001", "abc"));
        // 2つの誤りが集まる
        assertEquals(2, errors.size());
        // 1つ目は顧客IDの誤り
        assertEquals("customerId", errors.get(0).field());
        // 2つ目は数量の誤り
        assertEquals("quantity", errors.get(1).field());
    }

    /** 入力の誤りは、決済まで進まない。 */
    @Test
    @DisplayName("入力の誤りは、入力の誤りとして返り、決済は呼ばれない")
    void invalidInputStopsAtTheEntrance() {
        // 承認する決済を用意する
        FakePaymentClient client = new FakePaymentClient(FakePaymentClient.Mode.APPROVE);
        // 範囲の外の数量で注文する
        PlaceOrderResult result = service(stockBook(), client).place(new OrderRequest("C001", "P001", "100"));
        // 入力の誤りになる
        assertTrue(result.isInvalid());
        // 決済は呼ばれていない
        assertEquals(0, client.calls());
    }

    /** データを見ないと分からない誤りは、業務の失敗になる。 */
    @Test
    @DisplayName("商品がない、在庫が足りない、上限金額を超える、は業務の失敗として種類つきで返る")
    void businessFailuresHaveKinds() {
        // 台帳を用意する
        StockBook book = stockBook();
        // 高い商品を登録する
        book.register("P900", 60_000, 10);
        // 承認する決済を用意する
        FakePaymentClient client = new FakePaymentClient(FakePaymentClient.Mode.APPROVE);
        // サービスを作る
        OrderService service = service(book, client);
        // ない商品は、商品なし
        assertEquals(OrderFailure.UNKNOWN_PRODUCT, service.place(new OrderRequest("C001", "P404", "1")).failure());
        // 在庫より多い数は、在庫不足
        assertEquals(OrderFailure.OUT_OF_STOCK, service.place(new OrderRequest("C001", "P001", "6")).failure());
        // 上限金額を超える注文は、上限超え
        assertEquals(OrderFailure.OVER_LIMIT, service.place(new OrderRequest("C001", "P900", "2")).failure());
        // どれも決済まで進まない
        assertEquals(0, client.calls());
    }

    /** カードの拒否は業務の失敗で、在庫は減らない。 */
    @Test
    @DisplayName("カードの拒否は業務の失敗として返り、在庫は減らない")
    void declinedPaymentKeepsStock() {
        // 台帳を用意する
        StockBook book = stockBook();
        // 断る決済で注文する
        PlaceOrderResult result = service(book, new FakePaymentClient(FakePaymentClient.Mode.DECLINE))
                .place(new OrderRequest("C001", "P001", "2"));
        // カードの拒否になる
        assertEquals(OrderFailure.PAYMENT_DECLINED, result.failure());
        // 在庫は5個のまま
        assertEquals(5, book.stockOf("P001"));
    }

    /** 通信の失敗は、境界で翻訳され、原因が残る。 */
    @Test
    @DisplayName("通信の失敗は PaymentUnavailableException に翻訳され、元の IOException が原因に残る")
    void technicalFailureIsTranslatedWithCause() {
        // 通信に失敗するゲートウェイを作る
        PaymentGateway gateway = new PaymentGateway(new FakePaymentClient(FakePaymentClient.Mode.DOWN));
        // 決済すると、この教材の例外になる
        PaymentUnavailableException e = assertThrows(PaymentUnavailableException.class,
                () -> gateway.charge("C001", 1000));
        // 元の例外が、原因として残っている
        assertInstanceOf(IOException.class, e.getCause());
    }

    /** 入口が、結果と例外を利用者向けの文章に変える。 */
    @Test
    @DisplayName("入口は、成功、入力の誤り、業務の失敗、決済の停止を、それぞれの文章に変える")
    void controllerTranslatesForUsers() {
        // 承認する決済で、入口を作る
        OrderController ok = new OrderController(service(stockBook(), new FakePaymentClient(FakePaymentClient.Mode.APPROVE)));
        // 成功の文章になる
        assertEquals("注文を受け付けました（O-1、2000円）", ok.handle(new OrderRequest("C001", "P001", "2")));
        // 入力の誤りは、誤りの説明になる
        assertEquals("数量は数字で入力してください", ok.handle(new OrderRequest("C001", "P001", "abc")));
        // 業務の失敗は、種類ごとの説明になる
        assertEquals("在庫が足りません。数量を減らしてください", ok.handle(new OrderRequest("C001", "P001", "9")));
        // 台帳を用意する
        StockBook book = stockBook();
        // 通信に失敗する決済で、入口を作る
        OrderController down = new OrderController(service(book, new FakePaymentClient(FakePaymentClient.Mode.DOWN)));
        // 決済の停止は、時間をおくよう促す文章になる
        assertTrue(down.handle(new OrderRequest("C001", "P001", "1")).startsWith("ただいま決済を利用できません"));
        // 在庫は減っていない
        assertEquals(5, book.stockOf("P001"));
    }

    /** プログラムの誤りは、隠さない。 */
    @Test
    @DisplayName("プログラムの誤りは隠さず、例外で止まる")
    void programmingErrorsAreNotHidden() {
        // 承認する決済で、入口を作る
        OrderController controller = new OrderController(service(stockBook(), new FakePaymentClient(FakePaymentClient.Mode.APPROVE)));
        // 入力そのものがないのは、例外で止まる
        assertThrows(NullPointerException.class, () -> controller.handle(null));
        // 検証を通さずに範囲外の数量を作るのも、例外で止まる
        assertThrows(IllegalArgumentException.class, () -> Quantity.of(0));
        // 成功の結果には、失敗の種類がない
        assertFalse(PlaceOrderResult.success("O-1", 1000).isInvalid());
    }
}
