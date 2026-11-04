package com.example.orders.ch16_template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.ch16_template.before.StoreOrderProcessor;
import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 第16章の分解前の店頭注文の仕様を確かめます。
 */
class StoreOrderProcessorBeforeTest {
    // 商品IDごとの在庫数を持つフィールド
    private Map<String, Integer> stock;
    // 出力の記録先を持つフィールド
    private ByteArrayOutputStream buffer;
    // テスト対象の注文処理を持つフィールド
    private StoreOrderProcessor processor;

    /** 各テストの前に、テスト対象と記録用の部品を作り直します。 */
    @BeforeEach
    void setUp() {
        // 毎回、在庫（P1は100個）と出力先を作り直す
        stock = new HashMap<>();
        // 在庫に、P1を100個登録する
        stock.put("P1", 100);
        // 出力を記録するための入れ物を、作り直す
        buffer = new ByteArrayOutputStream();
        // 店頭向けの処理を作る（出力は記録用の入れ物へ）
        processor = new StoreOrderProcessor(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /**
     * テスト用の注文を作ります。
     *
     * @param unitPrice 単価（円）
     * @param quantity 数量
     * @return 注文
     */
    private Order order(int unitPrice, int quantity) {
        // メールアドレスと電話番号のある一般会員を作る
        Customer customer = new Customer("C1", "山田", MemberRank.REGULAR, "a@example.com", "090");
        // P1を指定の単価と数量で注文する、テスト用の注文を作る
        return new Order("O1", customer, List.of(new OrderLine("P1", unitPrice, quantity)), null, LocalDate.of(2026, 5, 10));
    }

    /**
     * これまでの出力を、文字列で返します。
     *
     * @return 出力の内容
     */
    private String text() {
        // 記録した出力を、文字列にして返す
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** 店頭は50万円を超えても、100万円までなら決済できる。 */
    @Test
    @DisplayName("店頭は50万円を超えても、100万円までなら決済できる")
    void higherLimit() {
        // 店頭の決済上限は100万円。60万円の注文は、ウェブでは拒否されるが、店頭では通る
        OrderResult r = processor.process(order(100000, 6));
        // 請求額が60万円であることを確かめる
        assertEquals(600000, r.total());
    }

    /** 店頭でも、100万円を超えると決済拒否になり、在庫が戻る。 */
    @Test
    @DisplayName("店頭でも、100万円を超えると決済拒否になり、在庫が戻る")
    void declinedOverLimit() {
        // 110万円の注文は、店頭の上限も超える。拒否され、引き当てた在庫は元に戻る
        assertThrows(PaymentDeclinedException.class, () -> processor.process(order(100000, 11)));
        // P1の在庫が、100個のまま減っていないことを確かめる
        assertEquals(100, stock.get("P1"));
    }

    /** 店頭ではメールもSMSも送らず、監査ログに店頭の印が付く。 */
    @Test
    @DisplayName("店頭ではメールもSMSも送らず、監査ログに店頭の印が付く")
    void noNotificationAndStoreAudit() {
        // 店頭の客には、その場で伝えるので、メールもSMSも送らない。監査ログには、チャネル（STORE）が付く
        processor.process(order(5000, 2));
        // メール通知が出力されていないことを確かめる
        assertFalse(text().contains("[MAIL]"));
        // SMS通知が出力されていないことを確かめる
        assertFalse(text().contains("[SMS]"));
        // 決済ログが出力されることを確かめる
        assertTrue(text().contains("[PAYMENT] order=O1 amount=10000"));
        // 店頭を表す印の付いた監査ログが出力されることを確かめる
        assertTrue(text().contains("[AUDIT] order=O1 customer=C1 total=10000 channel=STORE"));
    }
}
