package com.example.orders.ch02_roles;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * クラスが役割ごとに分かれていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * 入口（Controller）→ 業務ロジック（Service）→ 基盤寄りの処理（Repository など）
 * 値の運び役（Dto）は、どの役割からも使われる
 * </pre>
 */
class RolesStructureTest {
    // DIRを持つフィールド
    private static final String DIR = "ch02_roles/exercise";

    /** 4つの役割（Controller / Service / Repository / Dto）のクラスがある。 */
    @Test
    @DisplayName("4つの役割（Controller / Service / Repository / Dto）のクラスがある")
    void fourRolesExist() {
        // 名前が Controller で終わるクラスがあることを確かめる
        SourceRules.assertClassWithSuffixExists(DIR, "Controller");
        // 名前が Service で終わるクラスがあることを確かめる
        SourceRules.assertClassWithSuffixExists(DIR, "Service");
        // 名前が Repository で終わるクラスがあることを確かめる
        SourceRules.assertClassWithSuffixExists(DIR, "Repository");
        // 名前が Dto で終わるクラスがあることを確かめる
        SourceRules.assertClassWithSuffixExists(DIR, "Dto");
    }

    /** 入口（Controller）は、業務ルールもデータの保持も持たない。 */
    @Test
    @DisplayName("入口（Controller）は、業務ルールもデータの保持も持たない")
    void controllerHasNoRulesAndNoData() {
        // ★重要★ 入口は、入力を整えて Service を呼ぶだけ。ルールもデータも持たない
        SourceRules.assertClassWithSuffixExists(DIR, "Controller");
        // ★重要★ Controllerに置いてはいけないものを含まないことを確かめる（決済上限は業務ルールです。Service に置きます）
        SourceRules.assertNoTextInClasses(DIR, "Controller", "500000", "決済上限は業務ルールです。Service に置きます");
        // ★重要★ Controllerに置いてはいけないものを含まないことを確かめる（割引は業務ルールです。Service に置きます）
        SourceRules.assertNoTextInClasses(DIR, "Controller", "* 10 / 100", "割引は業務ルールです。Service に置きます");
        // ★重要★ Controllerに置いてはいけないものを含まないことを確かめる（データの保持は Repository の役割です）
        SourceRules.assertNoTextInClasses(DIR, "Controller", "HashMap", "データの保持は Repository の役割です");
    }

    /** 業務ロジック（Service）は、コマンドの解釈も画面への出力もしない。 */
    @Test
    @DisplayName("業務ロジック（Service）は、コマンドの解釈も画面への出力もしない")
    void serviceKnowsNeitherInputNorOutput() {
        // ★重要★ 業務ロジックは、入力の形式にも、出力の方法にも依存しない
        SourceRules.assertClassWithSuffixExists(DIR, "Service");
        // ★重要★ Serviceに置いてはいけないものを含まないことを確かめる（コマンドの解釈は Controller の役割です）
        SourceRules.assertNoTextInClasses(DIR, "Service", ".split(", "コマンドの解釈は Controller の役割です");
        // ★重要★ Serviceに置いてはいけないものを含まないことを確かめる（出力は基盤寄りのクラスの役割です）
        SourceRules.assertNoTextInClasses(DIR, "Service", "println", "出力は基盤寄りのクラスの役割です");
        // ★重要★ Serviceに置いてはいけないものを含まないことを確かめる（出力は基盤寄りのクラスの役割です）
        SourceRules.assertNoTextInClasses(DIR, "Service", "PrintStream", "出力は基盤寄りのクラスの役割です");
    }

    /** 基盤寄りの処理（Repository）は、業務ルールを持たない。 */
    @Test
    @DisplayName("基盤寄りの処理（Repository）は、業務ルールを持たない")
    void repositoryHasNoRules() {
        // ★重要★ 基盤寄りの処理は、データの読み書きだけを担う。ルールは Service に置く
        SourceRules.assertClassWithSuffixExists(DIR, "Repository");
        // ★重要★ Repositoryに置いてはいけないものを含まないことを確かめる（決済上限は業務ルールです）
        SourceRules.assertNoTextInClasses(DIR, "Repository", "500000", "決済上限は業務ルールです");
        // ★重要★ Repositoryに置いてはいけないものを含まないことを確かめる（割引は業務ルールです）
        SourceRules.assertNoTextInClasses(DIR, "Repository", "* 10 / 100", "割引は業務ルールです");
        // ★重要★ Repositoryに置いてはいけないものを含まないことを確かめる（出力はここに置きません）
        SourceRules.assertNoTextInClasses(DIR, "Repository", "println", "出力はここに置きません");
    }
}
