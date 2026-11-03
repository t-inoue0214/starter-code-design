package com.example.orders.ch11_cqrs;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 更新側（command）と参照側（query）が、互いを知らずに分かれていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 */
class CqrsStructureTest {
    private static final String ROOT = "ch11_cqrs/exercise/";

    @Test
    @DisplayName("更新側（command）と参照側（query）のパッケージがある")
    void packagesExist() {
        SourceRules.assertPackageExists(ROOT + "command");
        SourceRules.assertPackageExists(ROOT + "query");
    }

    @Test
    @DisplayName("更新側は、参照側を知らない")
    void commandDoesNotKnowQuery() {
        SourceRules.assertExistsAndNoImport(ROOT + "command", ".exercise.query.");
    }

    @Test
    @DisplayName("参照側は、更新側を知らない")
    void queryDoesNotKnowCommand() {
        SourceRules.assertExistsAndNoImport(ROOT + "query", ".exercise.command.");
    }

    @Test
    @DisplayName("更新側は、通知などの出力（java.io）を持たず、ルールと更新に集中している")
    void commandHasNoOutput() {
        SourceRules.assertExistsAndNoImport(ROOT + "command", "java.io.");
    }
}
