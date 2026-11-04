package com.example.orders.ch06_mvc;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Model・View・Controller の役割が分かれ、依存の向きが守られていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * Controller → Model、Controller → View、View → Model（読むだけ）
 * Model は、View も Controller も、画面への出力も知らない
 * </pre>
 */
class MvcStructureTest {
    // ROOTを持つフィールド
    private static final String ROOT = "ch06_mvc/exercise/";

    /** model・view・controller の3つのパッケージがある。 */
    @Test
    @DisplayName("model・view・controller の3つのパッケージがある")
    void threePackagesExist() {
        // model パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "model");
        // view パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "view");
        // controller パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "controller");
    }

    /** Model は、View も Controller も知らない。 */
    @Test
    @DisplayName("Model は、View も Controller も知らない")
    void modelKnowsNeitherViewNorController() {
        // ★重要★ model が「.exercise.view.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "model", ".exercise.view.");
        // ★重要★ model が「.exercise.controller.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "model", ".exercise.controller.");
    }

    /** Model は、画面への出力（java.io）を知らない。 */
    @Test
    @DisplayName("Model は、画面への出力（java.io）を知らない")
    void modelHasNoOutput() {
        // ★重要★ model が「java.io.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "model", "java.io.");
    }

    /** View は、Controller を知らない。 */
    @Test
    @DisplayName("View は、Controller を知らない")
    void viewDoesNotKnowController() {
        // ★重要★ view が「.exercise.controller.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "view", ".exercise.controller.");
    }

    /** Controller は、画面への出力（java.io）を直接扱わない（出力は View に任せる）。 */
    @Test
    @DisplayName("Controller は、画面への出力（java.io）を直接扱わない（出力は View に任せる）")
    void controllerExists() {
        // ★重要★ controller が「java.io.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "controller", "java.io.");
    }
}
