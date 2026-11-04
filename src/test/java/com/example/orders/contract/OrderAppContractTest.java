package com.example.orders.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.orders.domain.OrderApp;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 注文アプリ（コマンドを受けて応答を返す）の「振る舞いの契約」を確かめる共通テストです。
 * 第4〜7章と第10・11章のテストはこのクラスを継承し、{@link #create} だけを実装します。
 * 構造を変えても、このテストを書き換えないことが、リファクタリングの正しさの証明になります。
 */
public abstract class OrderAppContractTest {
    // 商品IDごとの在庫数を持つフィールド
    protected Map<String, Integer> stock;
    // 出力の記録先を持つフィールド
    private ByteArrayOutputStream buffer;
    // テスト対象の注文アプリを持つフィールド
    private OrderApp app;

    /**
     * テスト対象の注文アプリを生成します。
     *
     * @param stock 商品ごとの在庫数
     * @param out   通知を出力する先
     * @param today 今日の日付
     * @return テスト対象
     */
    protected abstract OrderApp create(Map<String, Integer> stock, PrintStream out, LocalDate today);

    /** 各テストの前に、テスト対象と記録用の部品を作り直します。 */
    @BeforeEach
    void setUp() {
        // 毎回、在庫（P1は100個、P2は5個）と出力先を作り直し、今日を2026年5月10日とする
        stock = new HashMap<>();
        // 在庫に、P1を100個登録する
        stock.put("P1", 100);
        // 在庫に、P2を5個登録する
        stock.put("P2", 5);
        // 出力を記録するための入れ物を、作り直す
        buffer = new ByteArrayOutputStream();
        // テスト対象のアプリを作る（今日は2026年5月10日、出力は記録用の入れ物へ）
        app = create(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8), LocalDate.of(2026, 5, 10));
    }

    /**
     * これまでの出力を、文字列で返します。
     *
     * @return 出力の内容
     */
    private String output() {
        // 記録した出力を、文字列にして返す
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** 注文を受け付けると、請求額を返して在庫を引き当てる。 */
    @Test
    @DisplayName("注文を受け付けると、請求額を返して在庫を引き当てる")
    void placeSucceeds() {
        // 一般会員が1000円 × 10個。割引なしで10000円になり、在庫は100個から90個に減る
        assertEquals("OK O1 total=10000", app.handle("PLACE O1 C1 REGULAR P1:1000:10"));
        // P1の在庫が 90 個になることを確かめる
        assertEquals(90, stock.get("P1"));
    }

    /** ゴールド会員は10%、シルバー会員は5%割引される。 */
    @Test
    @DisplayName("ゴールド会員は10%、シルバー会員は5%割引される")
    void rankDiscount() {
        // ゴールドは10%引きの9000円、シルバーは5%引きの9500円
        assertEquals("OK O1 total=9000", app.handle("PLACE O1 C1 GOLD P1:1000:10"));
        // コマンド「PLACE O2 C2 SILVER P1:1000:10」の応答が「OK O2 total=9500」になることを確かめる
        assertEquals("OK O2 total=9500", app.handle("PLACE O2 C2 SILVER P1:1000:10"));
    }

    /** 複数の明細を受け付けられる。 */
    @Test
    @DisplayName("複数の明細を受け付けられる")
    void multipleLines() {
        // 明細は ; でつなぐ。P1を3個、P2を1個注文し、それぞれの在庫が減る
        assertEquals("OK O1 total=3500", app.handle("PLACE O1 C1 REGULAR P1:1000:3;P2:500:1"));
        // P1の在庫が 97 個になることを確かめる
        assertEquals(97, stock.get("P1"));
        // P2の在庫が 4 個になることを確かめる
        assertEquals(4, stock.get("P2"));
    }

    /** 成功すると通知が出力される。 */
    @Test
    @DisplayName("成功すると通知が出力される")
    void notifies() {
        // 成立した注文は、顧客と注文IDと請求額を含む通知を出力する
        app.handle("PLACE O1 C1 GOLD P1:1000:10");
        // 出力に「[NOTIFY] customer=C1 order=O1 total=9000」が含まれることを確かめる
        assertTrue(output().contains("[NOTIFY] customer=C1 order=O1 total=9000"));
    }

    /** 在庫が足りなければエラーを返し、在庫は減らず、通知もされない。 */
    @Test
    @DisplayName("在庫が足りなければエラーを返し、在庫は減らず、通知もされない")
    void outOfStock() {
        // P1は足りるが P2 が足りない。エラー応答になり、在庫は減らず、通知も出ない
        assertEquals("ERROR 在庫が不足しています: P2", app.handle("PLACE O1 C1 REGULAR P1:1000:3;P2:500:6"));
        // P1の在庫が、100個のまま減っていないことを確かめる
        assertEquals(100, stock.get("P1"));
        // P2の在庫が、5個のまま減っていないことを確かめる
        assertEquals(5, stock.get("P2"));
        // 出力に「[NOTIFY]」が含まれないことを確かめる
        assertFalse(output().contains("[NOTIFY]"));
    }

    /** 50万円を超える請求は決済拒否となり、在庫が戻る。 */
    @Test
    @DisplayName("50万円を超える請求は決済拒否となり、在庫が戻る")
    void paymentDeclined() {
        // 60万円の請求は上限の50万円を超える。拒否され、引き当てた在庫は戻る
        assertEquals("ERROR 決済が拒否されました: O1", app.handle("PLACE O1 C1 REGULAR P1:100000:6"));
        // P1の在庫が、100個のまま減っていないことを確かめる
        assertEquals(100, stock.get("P1"));
    }

    /** 注文IDが重複していればエラー。 */
    @Test
    @DisplayName("注文IDが重複していればエラー")
    void duplicateOrderId() {
        // 同じ注文IDで2回注文すると、2回目はエラーになる
        app.handle("PLACE O1 C1 REGULAR P1:1000:1");
        // コマンド「PLACE O1 C1 REGULAR P1:1000:1」の応答が「ERROR 注文IDが重複しています: O1」になることを確かめる
        assertEquals("ERROR 注文IDが重複しています: O1", app.handle("PLACE O1 C1 REGULAR P1:1000:1"));
    }

    /** 不明なコマンドと、形式が不正なコマンドはエラー。 */
    @Test
    @DisplayName("不明なコマンドと、形式が不正なコマンドはエラー")
    void badCommands() {
        // 未知のコマンド、項目数の不足、数値でない単価の3通りがエラーになる
        assertEquals("ERROR 不明なコマンドです: FOO", app.handle("FOO bar"));
        // コマンド「PLACE O1 C1」の応答が「ERROR 形式が不正です」になることを確かめる
        assertEquals("ERROR 形式が不正です", app.handle("PLACE O1 C1"));
        // コマンド「PLACE O1 C1 REGULAR P1:abc:1」の応答が「ERROR 形式が不正です」になることを確かめる
        assertEquals("ERROR 形式が不正です", app.handle("PLACE O1 C1 REGULAR P1:abc:1"));
    }

    /** SHOW で注文を参照できる。存在しなければエラー。 */
    @Test
    @DisplayName("SHOW で注文を参照できる。存在しなければエラー")
    void show() {
        // 成立した注文は SHOW で参照できる。存在しない注文IDはエラーになる
        app.handle("PLACE O1 C1 GOLD P1:1000:10");
        // SHOW で、注文の内容が応答に出ることを確かめる
        assertEquals("ORDER O1 customer=C1 total=9000", app.handle("SHOW O1"));
        // コマンド「SHOW O9」の応答が「ERROR 注文が見つかりません: O9」になることを確かめる
        assertEquals("ERROR 注文が見つかりません: O9", app.handle("SHOW O9"));
    }

    /** HISTORY で顧客の注文履歴を参照できる（失敗した注文は含まない）。 */
    @Test
    @DisplayName("HISTORY で顧客の注文履歴を参照できる（失敗した注文は含まない）")
    void history() {
        // O3 は在庫不足で失敗するので履歴に載らない。O4 は別の顧客の注文
        app.handle("PLACE O1 C1 REGULAR P1:1000:1");
        // コマンド「PLACE O2 C1 REGULAR P1:1000:1」を実行する
        app.handle("PLACE O2 C1 REGULAR P1:1000:1");
        // コマンド「PLACE O3 C1 REGULAR P2:500:99」を実行する
        app.handle("PLACE O3 C1 REGULAR P2:500:99");
        // コマンド「PLACE O4 C2 REGULAR P1:1000:1」を実行する
        app.handle("PLACE O4 C2 REGULAR P1:1000:1");
        // コマンド「HISTORY C1」の応答が「HISTORY C1 O1,O2」になることを確かめる
        assertEquals("HISTORY C1 O1,O2", app.handle("HISTORY C1"));
        // コマンド「HISTORY C9」の応答が「HISTORY C9 (none)」になることを確かめる
        assertEquals("HISTORY C9 (none)", app.handle("HISTORY C9"));
    }
}
