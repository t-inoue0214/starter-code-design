package com.example.orders.ch10_hexagonal;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    private static final String[] CORE_DIRS = {"core", "core/port/inbound", "core/port/outbound"};

    @Test
    @DisplayName("中心（core）とアダプタ（入口・出口）のパッケージがある")
    void packagesExist() {
        SourceRules.assertPackageExists(ROOT + "core");
        SourceRules.assertPackageExists(ROOT + "adapter/inbound");
        SourceRules.assertPackageExists(ROOT + "adapter/outbound");
    }

    @Test
    @DisplayName("中心は、アダプタを知らない")
    void coreDoesNotKnowAdapters() {
        for (String dir : CORE_DIRS) {
            SourceRules.assertExistsAndNoImport(ROOT + dir, ".exercise.adapter.");
        }
    }

    @Test
    @DisplayName("中心は、入出力（java.io）を知らない")
    void coreHasNoIo() {
        for (String dir : CORE_DIRS) {
            SourceRules.assertExistsAndNoImport(ROOT + dir, "java.io.");
        }
    }

    @Test
    @DisplayName("出口のアダプタは、中心が定義した出口のポートを実装している（依存の向きが中心へ向く）")
    void outboundAdaptersDependOnCorePorts() {
        SourceRules.assertSomeFileImports(ROOT + "adapter/outbound", ".exercise.core.port.outbound.");
    }

    @Test
    @DisplayName("入口のアダプタは、中心の入口のポートを使っている")
    void inboundAdapterUsesInboundPort() {
        SourceRules.assertSomeFileImports(ROOT + "adapter/inbound", ".exercise.core.port.inbound.");
    }
}
