package com.example.orders.ch08_layered;

import com.example.orders.contract.SourceRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 3つの層に分かれ、依存の向きが上から下に守られていることを確かめるテストです。
 * 課題を終えるまでは失敗します（それが課題のゴールです）。
 *
 * <pre>
 * presentation（表示層） → service（業務層） → persistence（永続化層）
 * </pre>
 */
class LayeredStructureTest {
    private static final String ROOT = "ch08_layered/exercise/";

    @Test
    @DisplayName("表示層・業務層・永続化層の3つのパッケージがある")
    void threeLayersExist() {
        SourceRules.assertPackageExists(ROOT + "presentation");
        SourceRules.assertPackageExists(ROOT + "service");
        SourceRules.assertPackageExists(ROOT + "persistence");
    }

    @Test
    @DisplayName("表示層は、永続化層を直接使わない（業務層を経由する）")
    void presentationDoesNotKnowPersistence() {
        SourceRules.assertExistsAndNoImport(ROOT + "presentation", ".exercise.persistence.");
    }

    @Test
    @DisplayName("業務層は、表示層を知らない（下から上への依存がない）")
    void serviceDoesNotKnowPresentation() {
        SourceRules.assertExistsAndNoImport(ROOT + "service", ".exercise.presentation.");
    }

    @Test
    @DisplayName("永続化層は、業務層も表示層も知らない")
    void persistenceKnowsNoUpperLayer() {
        SourceRules.assertExistsAndNoImport(ROOT + "persistence", ".exercise.service.");
        SourceRules.assertExistsAndNoImport(ROOT + "persistence", ".exercise.presentation.");
    }
}
