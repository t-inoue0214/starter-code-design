package com.example.orders.ch05_ecb;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Boundary / Control / Entity の3つの役割に分かれ、依存の向きが守られていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * boundary（外との接点） → control（処理の流れ） → entity（業務の概念とルール）
 * </pre>
 */
class EcbStructureTest {
    private static final String ROOT = "ch05_ecb/exercise/";

    /** boundary・control・entity の3つのパッケージがある。 */
    @Test
    @DisplayName("boundary・control・entity の3つのパッケージがある")
    void threeRolesExist() {
        SourceRules.assertPackageExists(ROOT + "boundary");
        SourceRules.assertPackageExists(ROOT + "control");
        SourceRules.assertPackageExists(ROOT + "entity");
    }

    /** boundary は control を使い、entity を直接使わない。 */
    @Test
    @DisplayName("boundary は control を使い、entity を直接使わない")
    void boundaryTalksOnlyToControl() {
        // ★重要★ Boundary は Control を呼ぶ。Entity は直接使わない（必要な値は Control が詰め直して返す）
        SourceRules.assertSomeFileImports(ROOT + "boundary", ".exercise.control.");
        SourceRules.assertNoImport(ROOT + "boundary", ".exercise.entity.");
    }

    /** control は entity を使い、boundary を知らない。 */
    @Test
    @DisplayName("control は entity を使い、boundary を知らない")
    void controlKnowsEntityNotBoundary() {
        // ★重要★ Control は Entity を使う。外との接点（Boundary）の都合は知らない
        SourceRules.assertSomeFileImports(ROOT + "control", ".exercise.entity.");
        SourceRules.assertNoImport(ROOT + "control", ".exercise.boundary.");
    }

    /** entity は、control も boundary も知らない。 */
    @Test
    @DisplayName("entity は、control も boundary も知らない")
    void entityKnowsNoOtherRole() {
        SourceRules.assertExistsAndNoImport(ROOT + "entity", ".exercise.control.");
        SourceRules.assertExistsAndNoImport(ROOT + "entity", ".exercise.boundary.");
    }

    /** boundary は、業務ルールも状態の保持も持たない。 */
    @Test
    @DisplayName("boundary は、業務ルールも状態の保持も持たない")
    void boundaryHasNoRulesAndNoState() {
        // ★重要★ ルール（決済上限など）と状態（Map など）は、Entity が持つ
        SourceRules.assertPackageExists(ROOT + "boundary");
        SourceRules.assertNoTextInPackage(ROOT + "boundary", "500000", "決済上限は業務ルールです。entity に置きます");
        SourceRules.assertNoTextInPackage(ROOT + "boundary", "HashMap", "状態の保持は entity の役割です");
    }
}
