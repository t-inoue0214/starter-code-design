package com.example.orders.ch10_hexagonal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.contract.SourceRules;

/**
 * 業務の中心が外の世界に依存せず、外の世界が中心に依存していることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * adapter（外） → core（中心）。core は adapter を知らない
 * 出口のポートは core が定義し、adapter が実装する（依存の向きが逆転する）
 * </pre>
 */
class HexagonalStructureTest {
    private static final String ROOT = "ch10_hexagonal/exercise/";
    private static final String[] CORE_DIRS = { "core", "core/port/inbound", "core/port/outbound" };

    /** 中心（core）とアダプタ（入口・出口）のパッケージがある。 */
    @Test
    @DisplayName("中心（core）とアダプタ（入口・出口）のパッケージがある")
    void packagesExist() {
        SourceRules.assertPackageExists(ROOT + "core");
        SourceRules.assertPackageExists(ROOT + "adapter/inbound");
        SourceRules.assertPackageExists(ROOT + "adapter/outbound");
    }

    /** 中心は、アダプタを知らない。 */
    @Test
    @DisplayName("中心は、アダプタを知らない")
    void coreDoesNotKnowAdapters() {
        // ★重要★ 依存は、外（アダプタ）から中心へ向かう。中心がアダプタを import していれば、向きが逆になっている
        for (String dir : CORE_DIRS) {
            SourceRules.assertExistsAndNoImport(ROOT + dir, ".exercise.adapter.");
        }
    }

    /** 中心は、入出力（java.io）を知らない。 */
    @Test
    @DisplayName("中心は、入出力（java.io）を知らない")
    void coreHasNoIo() {
        // ★重要★ 入出力（java.io）は外の世界の事情なので、中心には置かない
        for (String dir : CORE_DIRS) {
            SourceRules.assertExistsAndNoImport(ROOT + dir, "java.io.");
        }
    }

    /** 出口のアダプタは、中心が定義した出口のポートを実装している（依存の向きが中心へ向く）。 */
    @Test
    @DisplayName("出口のアダプタは、中心が定義した出口のポートを実装している（依存の向きが中心へ向く）")
    void outboundAdaptersDependOnCorePorts() {
        // ★重要★ 出口のアダプタが、中心の定めたポートを実装する。ポートの定義者は中心である
        SourceRules.assertSomeFileImports(ROOT + "adapter/outbound", ".exercise.core.port.outbound.");
    }

    /** 入口のアダプタは、中心の入口のポートを使っている。 */
    @Test
    @DisplayName("入口のアダプタは、中心の入口のポートを使っている")
    void inboundAdapterUsesInboundPort() {
        // ★重要★ 入口のアダプタが、中心の入口のポートを通して、中心を呼ぶ
        SourceRules.assertSomeFileImports(ROOT + "adapter/inbound", ".exercise.core.port.inbound.");
    }
}
