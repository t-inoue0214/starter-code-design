package com.example.orders.ch04_layered;

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
    // ROOTを持つフィールド
    private static final String ROOT = "ch04_layered/exercise/";

    /** 表示層・業務層・永続化層の3つのパッケージがある。 */
    @Test
    @DisplayName("表示層・業務層・永続化層の3つのパッケージがある")
    void threeLayersExist() {
        // presentation パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "presentation");
        // service パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "service");
        // persistence パッケージに、クラスがあることを確かめる
        SourceRules.assertPackageExists(ROOT + "persistence");
    }

    /** 表示層は、永続化層を直接使わない（業務層を経由する）。 */
    @Test
    @DisplayName("表示層は、永続化層を直接使わない（業務層を経由する）")
    void presentationDoesNotKnowPersistence() {
        // ★重要★ presentation が「.exercise.persistence.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "presentation", ".exercise.persistence.");
    }

    /** 業務層は、表示層を知らない（下から上への依存がない）。 */
    @Test
    @DisplayName("業務層は、表示層を知らない（下から上への依存がない）")
    void serviceDoesNotKnowPresentation() {
        // ★重要★ service が「.exercise.presentation.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "service", ".exercise.presentation.");
    }

    /** 永続化層は、業務層も表示層も知らない。 */
    @Test
    @DisplayName("永続化層は、業務層も表示層も知らない")
    void persistenceKnowsNoUpperLayer() {
        // ★重要★ persistence が「.exercise.service.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "persistence", ".exercise.service.");
        // ★重要★ persistence が「.exercise.presentation.」を import していないことを確かめる（依存の向きの検査）
        SourceRules.assertExistsAndNoImport(ROOT + "persistence", ".exercise.presentation.");
    }
}
