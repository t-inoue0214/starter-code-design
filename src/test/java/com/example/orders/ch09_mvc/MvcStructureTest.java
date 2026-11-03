package com.example.orders.ch09_mvc;

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
    private static final String ROOT = "ch09_mvc/exercise/";

    @Test
    @DisplayName("model・view・controller の3つのパッケージがある")
    void threePackagesExist() {
        SourceRules.assertPackageExists(ROOT + "model");
        SourceRules.assertPackageExists(ROOT + "view");
        SourceRules.assertPackageExists(ROOT + "controller");
    }

    @Test
    @DisplayName("Model は、View も Controller も知らない")
    void modelKnowsNeitherViewNorController() {
        SourceRules.assertExistsAndNoImport(ROOT + "model", ".exercise.view.");
        SourceRules.assertExistsAndNoImport(ROOT + "model", ".exercise.controller.");
    }

    @Test
    @DisplayName("Model は、画面への出力（java.io）を知らない")
    void modelHasNoOutput() {
        SourceRules.assertExistsAndNoImport(ROOT + "model", "java.io.");
    }

    @Test
    @DisplayName("View は、Controller を知らない")
    void viewDoesNotKnowController() {
        SourceRules.assertExistsAndNoImport(ROOT + "view", ".exercise.controller.");
    }

    @Test
    @DisplayName("Controller は、画面への出力（java.io）を直接扱わない（出力は View に任せる）")
    void controllerExists() {
        SourceRules.assertExistsAndNoImport(ROOT + "controller", "java.io.");
    }
}
