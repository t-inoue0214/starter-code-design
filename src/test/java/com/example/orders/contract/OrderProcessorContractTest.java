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

    /** 各テストの前に、テスト対象と記録用の部品を作り直します。 */
    @BeforeEach
    void setUp() {
        // 毎回、在庫（P1は100個、P2は5個）と出力先を作り直す。テスト同士が影響し合わないようにする
        stock = new HashMap<>();
        stock.put("P1", 100);
        stock.put("P2", 5);
        buffer = new ByteArrayOutputStream();
        processor = create(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    /**
     * これまでの出力を、文字列で返します。
     *
     * @return 出力の内容
     */
    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * テスト用の顧客を作ります。
     *
     * @param rank 会員ランク
     * @param email メールアドレス
     * @param phone 電話番号
     * @return 顧客
     */
    private Customer customer(MemberRank rank, String email, String phone) {
        return new Customer("C1", "山田", rank, email, phone);
    }

    /**
     * テスト用の注文を作ります。
     *
     * @param customer 顧客
     * @param unitPrice 単価（円）
     * @param quantity 数量
     * @param coupon クーポンコード（なければ null）
     * @param date 注文日
     * @return 注文
     */
    private Order order(Customer customer, int unitPrice, int quantity, String coupon, LocalDate date) {
        return new Order("O1", customer, List.of(new OrderLine("P1", unitPrice, quantity)), coupon, date);
    }

    // 12月ではない日付。季節割引の影響を受けない
    private static final LocalDate NORMAL_DAY = LocalDate.of(2026, 5, 10);

    /** 一般会員・クーポンなしは割引なしで請求する。 */
    @Test
    @DisplayName("一般会員・クーポンなしは割引なしで請求する")
    void regularNoDiscount() {
        // 一般会員・クーポンなし・通常の日付。割引の条件がどれも当てはまらない注文（1000円 × 3個）
        OrderResult r = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 3, null, NORMAL_DAY));
        assertEquals(3000, r.subtotal());
        assertEquals(0, r.discount());
        assertEquals(3000, r.total());
    }

    /** ゴールド会員は10%、シルバー会員は5%割引される。 */
    @Test
    @DisplayName("ゴールド会員は10%、シルバー会員は5%割引される")
    void rankDiscount() {
        // ゴールドは小計10000円の10%（1000円）、シルバーは小計1000円の5%（50円）
        OrderResult gold = processor.process(order(customer(MemberRank.GOLD, null, null), 1000, 10, null, NORMAL_DAY));
        assertEquals(1000, gold.discount());
        OrderResult silver = processor.process(order(customer(MemberRank.SILVER, null, null), 1000, 1, null, NORMAL_DAY));
        assertEquals(50, silver.discount());
    }

    /** クーポンは WELCOME500 が500円引き、SAVE10 が10%引き。 */
    @Test
    @DisplayName("クーポンは WELCOME500 が500円引き、SAVE10 が10%引き")
    void couponDiscount() {
        // WELCOME500 は定額の500円引き、SAVE10 は小計の10%引き
        OrderResult welcome = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, "WELCOME500", NORMAL_DAY));
        assertEquals(1500, welcome.total());
        OrderResult save = processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, "SAVE10", NORMAL_DAY));
        assertEquals(1800, save.total());
    }

    /** 12月は3%の季節割引が加わり、他の割引と合算される。 */
    @Test
    @DisplayName("12月は3%の季節割引が加わり、他の割引と合算される")
    void seasonalDiscount() {
        // ゴールド10% ＋ SAVE10 の10% ＋ 12月の3% で、小計10000円の23%（2300円）になる
        OrderResult r = processor.process(order(customer(MemberRank.GOLD, null, null), 1000, 10, "SAVE10", LocalDate.of(2026, 12, 20)));
        assertEquals(10000, r.subtotal());
        assertEquals(2300, r.discount());
        assertEquals(7700, r.total());
    }

    /** 割引は小計を超えない。 */
    @Test
    @DisplayName("割引は小計を超えない")
    void discountIsCapped() {
        // 小計300円に500円のクーポン。割引は300円までで止まり、請求額は0円になる
        OrderResult r = processor.process(order(customer(MemberRank.REGULAR, null, null), 300, 1, "WELCOME500", NORMAL_DAY));
        assertEquals(300, r.discount());
        assertEquals(0, r.total());
    }

    /** 未知のクーポンは検証エラー。 */
    @Test
    @DisplayName("未知のクーポンは検証エラー")
    void unknownCoupon() {
        // 登録されていないコードは、黙って無視せず、検証エラーにする
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 1, "NOPE", NORMAL_DAY)));
    }

    /** 数量0・明細なし・注文者なしは検証エラー。 */
    @Test
    @DisplayName("数量0・明細なし・注文者なしは検証エラー")
    void validation() {
        // 数量0、明細なし、注文者なしのそれぞれが、検証エラーになる
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 0, null, NORMAL_DAY)));
        assertThrows(OrderValidationException.class,
                () -> processor.process(new Order("O1", customer(MemberRank.REGULAR, null, null), List.of(), null, NORMAL_DAY)));
        assertThrows(OrderValidationException.class,
                () -> processor.process(order(null, 1000, 1, null, NORMAL_DAY)));
    }

    /** 在庫が足りなければ例外となり、在庫は減らない。 */
    @Test
    @DisplayName("在庫が足りなければ例外となり、在庫は減らない")
    void outOfStock() {
        // P1は足りるが P2 が足りない注文。全体が失敗し、足りていたP1の在庫も減らないことを確かめる
        Order o = new Order("O1", customer(MemberRank.REGULAR, null, null),
                List.of(new OrderLine("P1", 100, 3), new OrderLine("P2", 100, 6)), null, NORMAL_DAY);
        assertThrows(OutOfStockException.class, () -> processor.process(o));
        assertEquals(100, stock.get("P1"));
        assertEquals(5, stock.get("P2"));
    }

    /** 成功すると在庫が引き当てられる。 */
    @Test
    @DisplayName("成功すると在庫が引き当てられる")
    void stockReserved() {
        // 在庫100個から4個を引き当てると、96個になる
        processor.process(order(customer(MemberRank.REGULAR, null, null), 100, 4, null, NORMAL_DAY));
        assertEquals(96, stock.get("P1"));
    }

    /** 50万円を超える請求は決済拒否となり、在庫が戻る。 */
    @Test
    @DisplayName("50万円を超える請求は決済拒否となり、在庫が戻る")
    void paymentDeclined() {
        // 60万円の請求は上限の50万円を超える。拒否されたとき、引き当てた在庫は元に戻る
        assertThrows(PaymentDeclinedException.class,
                () -> processor.process(order(customer(MemberRank.REGULAR, null, null), 100000, 6, null, NORMAL_DAY)));
        assertEquals(100, stock.get("P1"));
    }

    /** メールは宛先があれば送られ、SMSは1万円以上かつ電話番号があるときだけ送られる。 */
    @Test
    @DisplayName("メールは宛先があれば送られ、SMSは1万円以上かつ電話番号があるときだけ送られる")
    void notifications() {
        // メールは宛先があれば送る。SMS は電話番号があり、かつ1万円以上のときだけ送る
        processor.process(order(customer(MemberRank.REGULAR, "a@example.com", "090"), 1000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[MAIL] to=a@example.com order=O1 total=2000"));
        assertFalse(output().contains("[SMS]"));

        processor.process(order(customer(MemberRank.REGULAR, null, "090"), 5000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[SMS] to=090 order=O1"));
    }

    /** 決済ログと監査ログが出力される。 */
    @Test
    @DisplayName("決済ログと監査ログが出力される")
    void paymentAndAudit() {
        // 成功した注文は、決済ログと監査ログの両方を残す
        processor.process(order(customer(MemberRank.REGULAR, null, null), 1000, 2, null, NORMAL_DAY));
        assertTrue(output().contains("[PAYMENT] order=O1 amount=2000"));
        assertTrue(output().contains("[AUDIT] order=O1 customer=C1 total=2000"));
    }
}
