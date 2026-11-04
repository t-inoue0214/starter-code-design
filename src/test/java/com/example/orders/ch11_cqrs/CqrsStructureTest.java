package com.example.orders.ch11_cqrs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.orders.contract.SourceRules;

/**
 * 更新側（command）と参照側（query）が、互いを知らずに分かれていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CqrsStructureTest {
    // ROOTを持つフィールド
    private static final String ROOT = "ch11_cqrs/exercise/";

    /** 更新側（command）と参照側（query）のパッケージがある。 */
    @Test
    @DisplayName("更新側（command）と参照側（query）のパッケージがある")
    void packagesExist() {
        // command パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "command");
        // query パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "query");
    }

    /** 更新側は、参照側を知らない。 */
    @Test
    @DisplayName("更新側は、参照側を知らない")
    void commandDoesNotKnowQuery() {
        // ★重要★ command が「.exercise.query.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "command", ".exercise.query.");
    }

    /** 参照側は、更新側を知らない。 */
    @Test
    @DisplayName("参照側は、更新側を知らない")
    void queryDoesNotKnowCommand() {
        // ★重要★ query が「.exercise.command.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "query", ".exercise.command.");
    }

    /** 更新側は、通知などの出力（java.io）を持たず、ルールと更新に集中している。 */
    @Test
    @DisplayName("更新側は、通知などの出力（java.io）を持たず、ルールと更新に集中している")
    void commandHasNoOutput() {
        // ★重要★ 出力の細部（java.io）は、更新側の外に置く。更新側は、ルールと状態の更新だけを担う
        SourceRules.assertExistsAndNoImport(ROOT + "command", "java.io.");
    }
}
