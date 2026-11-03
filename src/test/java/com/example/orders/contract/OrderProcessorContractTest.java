package com.example.orders.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.domain.Customer;
import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
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
 * 注文処理の「振る舞いの契約」を確かめる共通テストです。
 * 各章のテストはこのクラスを継承し、{@link #create} だけを実装します。
 * 分解の前後でこのテストを書き換えないことが、リファクタリングの正しさの証明になります。
 */
public abstract class OrderProcessorContractTest {
    private Map<String, Integer> stock;
    private ByteArrayOutputStream buffer;
    private OrderProcessor processor;

    /**
     * テスト対象の注文処理を生成します。
     *
     * @param stock 商品ごとの在庫数
     * @param out   通知・決済・監査ログの出力先
     * @return テスト対象
     */
    protected abstract OrderProcessor create(Map<String, Integer> stock, PrintStream out);

    @BeforeEach
    void setUp() {
        stock = new HashMap<>();
        stock.put("P1", 100);
        stock.put("P2", 5);
        buffer = new ByteArrayOutputStream();
        processor = create(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Customer customer(MemberRank rank, String email, String phone) {
        return new Customer("C1", "山田", rank, email, phone);
    }

    private Order order(Customer customer, int unitPrice, int quantity, String coupon, LocalDate date) {
        return new Order("O1", customer, List.of(new OrderLine("P1", unitPrice, quantity)), coupon, date);
    }

    private static final LocalDate NORMAL_DAY = LocalDate.of(2026, 5, 10);

    @Test
    @DisplayName("一般会員・クーポンなしは割引なしで請求する")
    void regularNoDiscount() {
        OrderResult r = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 3, null, NORMAL_DAY));
        assertEquals(3000, r.subtotal());
        assertEquals(0, r.discount());
        assertEquals(3000, r.total());
    }

    @Test
    @DisplayName("ゴールド会員は10%、シルバー会員は5%割引される")
    void rankDiscount() {
        OrderResult gold = processor.process(order(customer(MemberRank.GOLD, null, null), 1000, 10, null, NORMAL_DAY));
        assertEquals(1000, gold.discount());
        OrderResult silver = processor.process(order(customer(MemberRank.SILVER, null, null), 1000, 1, null, NORMAL_DAY));
        assertEquals(50, silver.discount());
    }

    @Test
    @DisplayName("クーポンは WELCOME500 が500円引き、SAVE10 が10%引き")
    void couponDiscount() {
        OrderResult welcome = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, "WELCOME500", NORMAL_DAY));
        assertEquals(1500, welcome.total());
        OrderResult save = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, "SAVE10", NORMAL_DAY));
        assertEquals(1800, save.total());
    }

    @Test
    @DisplayName("12月は3%の季節割引が加わり、他の割引と合算される")
    void seasonalDiscount() {
        OrderResult r = processor.process(order(customer(MemberRank.GOLD, null, null), 1000, 10, "SAVE10", LocalDate.of(2026, 12, 20)));
        assertEquals(10000, r.subtotal());
        assertEquals(2300, r.discount());
        assertEquals(7700, r.total());
    }

    @Test
    @DisplayName("割引は小計を超えない")
    void discountIsCapped() {
        OrderResult r = processor.process(order(customer(MemberRank.REGULAR, null, null), 300, 1, "WELCOME500", NORMAL_DAY));
        assertEquals(300, r.discount());
        assertEquals(0, r.total());
    }

    @Test
    @DisplayName("未知のクーポンは検証エラー")
    void unknownCoupon() {
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 1, "NOPE", NORMAL_DAY)));
    }

    @Test
    @DisplayName("数量0・明細なし・注文者なしは検証エラー")
    void validation() {
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 0, null, NORMAL_DAY)));
        assertThrows(OrderValidationException.class,
                () -> processor.process(new Order("O1", customer(MemberRank.REGULAR, null, null), List.of(), null, NORMAL_DAY)));
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(null, 1000, 1, null, NORMAL_DAY)));
    }

    @Test
    @DisplayName("在庫が足りなければ例外となり、在庫は減らない")
    void outOfStock() {
        Order o = new Order("O1", customer(MemberRank.REGULAR, null, null),
                List.of(new OrderLine("P1", 100, 3), new OrderLine("P2", 100, 6)), null, NORMAL_DAY);
        assertThrows(OutOfStockException.class, () -> processor.process(o));
        assertEquals(100, stock.get("P1"));
        assertEquals(5, stock.get("P2"));
    }

    @Test
    @DisplayName("成功すると在庫が引き当てられる")
    void stockReserved() {
        processor.process(order(customer(MemberRank.REGULAR, null, null), 100, 4, null, NORMAL_DAY));
        assertEquals(96, stock.get("P1"));
    }

    @Test
    @DisplayName("50万円を超える請求は決済拒否となり、在庫が戻る")
    void paymentDeclined() {
        assertThrows(PaymentDeclinedException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 100000, 6, null, NORMAL_DAY)));
        assertEquals(100, stock.get("P1"));
    }

    @Test
    @DisplayName("メールは宛先があれば送られ、SMSは1万円以上かつ電話番号があるときだけ送られる")
    void notifications() {
        processor.process(order(customer(MemberRank.REGULAR, "a@example.com", "090"), 1000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[MAIL] to=a@example.com order=O1 total=2000"));
        assertFalse(output().contains("[SMS]"));

        processor.process(order(customer(MemberRank.REGULAR, null, "090"), 5000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[SMS] to=090 order=O1"));
    }

    @Test
    @DisplayName("決済ログと監査ログが出力される")
    void paymentAndAudit() {
        processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[PAYMENT] order=O1 amount=2000"));
        assertTrue(output().contains("[AUDIT] order=O1 customer=C1 total=2000"));
    }
}
