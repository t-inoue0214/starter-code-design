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
 * 第8〜12章のテストはこのクラスを継承し、{@link #create} だけを実装します。
 * 構造を変えても、このテストを書き換えないことが、リファクタリングの正しさの証明になります。
 */
public abstract class OrderAppContractTest {
    protected Map<String, Integer> stock;
    private ByteArrayOutputStream buffer;
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

    @BeforeEach
    void setUp() {
        stock = new HashMap<>();
        stock.put("P1", 100);
        stock.put("P2", 5);
        buffer = new ByteArrayOutputStream();
        app = create(stock, new PrintStream(buffer, true, StandardCharsets.UTF_8), LocalDate.of(2026, 5, 10));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("注文を受け付けると、請求額を返して在庫を引き当てる")
    void placeSucceeds() {
        assertEquals("OK O1 total=10000", app.handle("PLACE O1 C1 REGULAR P1:1000:10"));
        assertEquals(90, stock.get("P1"));
    }

    @Test
    @DisplayName("ゴールド会員は10%、シルバー会員は5%割引される")
    void rankDiscount() {
        assertEquals("OK O1 total=9000", app.handle("PLACE O1 C1 GOLD P1:1000:10"));
        assertEquals("OK O2 total=9500", app.handle("PLACE O2 C2 SILVER P1:1000:10"));
    }

    @Test
    @DisplayName("複数の明細を受け付けられる")
    void multipleLines() {
        assertEquals("OK O1 total=3500", app.handle("PLACE O1 C1 REGULAR P1:1000:3;P2:500:1"));
        assertEquals(97, stock.get("P1"));
        assertEquals(4, stock.get("P2"));
    }

    @Test
    @DisplayName("成功すると通知が出力される")
    void notifies() {
        app.handle("PLACE O1 C1 GOLD P1:1000:10");
        assertTrue(output().contains("[NOTIFY] customer=C1 order=O1 total=9000"));
    }

    @Test
    @DisplayName("在庫が足りなければエラーを返し、在庫は減らず、通知もされない")
    void outOfStock() {
        assertEquals("ERROR 在庫が不足しています: P2", app.handle("PLACE O1 C1 REGULAR P1:1000:3;P2:500:6"));
        assertEquals(100, stock.get("P1"));
        assertEquals(5, stock.get("P2"));
        assertFalse(output().contains("[NOTIFY]"));
    }

    @Test
    @DisplayName("50万円を超える請求は決済拒否となり、在庫が戻る")
    void paymentDeclined() {
        assertEquals("ERROR 決済が拒否されました: O1", app.handle("PLACE O1 C1 REGULAR P1:100000:6"));
        assertEquals(100, stock.get("P1"));
    }

    @Test
    @DisplayName("注文IDが重複していればエラー")
    void duplicateOrderId() {
        app.handle("PLACE O1 C1 REGULAR P1:1000:1");
        assertEquals("ERROR 注文IDが重複しています: O1", app.handle("PLACE O1 C1 REGULAR P1:1000:1"));
    }

    @Test
    @DisplayName("不明なコマンドと、形式が不正なコマンドはエラー")
    void badCommands() {
        assertEquals("ERROR 不明なコマンドです: FOO", app.handle("FOO bar"));
        assertEquals("ERROR 形式が不正です", app.handle("PLACE O1 C1"));
        assertEquals("ERROR 形式が不正です", app.handle("PLACE O1 C1 REGULAR P1:abc:1"));
    }

    @Test
    @DisplayName("SHOW で注文を参照できる。存在しなければエラー")
    void show() {
        app.handle("PLACE O1 C1 GOLD P1:1000:10");
        assertEquals("ORDER O1 customer=C1 total=9000", app.handle("SHOW O1"));
        assertEquals("ERROR 注文が見つかりません: O9", app.handle("SHOW O9"));
    }

    @Test
    @DisplayName("HISTORY で顧客の注文履歴を参照できる（失敗した注文は含まない）")
    void history() {
        app.handle("PLACE O1 C1 REGULAR P1:1000:1");
        app.handle("PLACE O2 C1 REGULAR P1:1000:1");
        app.handle("PLACE O3 C1 REGULAR P2:500:99");
        app.handle("PLACE O4 C2 REGULAR P1:1000:1");
        assertEquals("HISTORY C1 O1,O2", app.handle("HISTORY C1"));
        assertEquals("HISTORY C9 (none)", app.handle("HISTORY C9"));
    }
}
