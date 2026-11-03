package com.example.orders.contract;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ソースコードの import を読んで、パッケージ間の依存の向きを検査する小さな道具です。
 * アーキテクチャパターンの章で、「層やモジュールの分け方が守られているか」を確かめるために使います。
 * 外部ライブラリは使わず、ファイルを読むだけで検査します。
 */
public final class SourceRules {
    private static final String BASE = "src/main/java/com/example/orders/";

    private SourceRules() {
    }

    /**
     * パッケージ（ディレクトリ）の直下にある Java ファイルを集めます。
     *
     * @param relativeDir 例: "ch08_layered/exercise/service"
     * @return Java ファイルの一覧（ディレクトリがなければ空）
     */
    public static List<Path> javaFiles(String relativeDir) {
        Path dir = Path.of(BASE + relativeDir);
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * パッケージに、Java ファイルが1つ以上あることを確かめます。
     *
     * @param relativeDir 例: "ch08_layered/exercise/service"
     */
    public static void assertPackageExists(String relativeDir) {
        assertFalse(javaFiles(relativeDir).isEmpty(),
                "パッケージが空です（クラスを作ってください）: " + relativeDir);
    }

    /**
     * パッケージ内のどのファイルも、禁止された import を含まないことを確かめます。
     *
     * @param relativeDir 検査するパッケージ（例: "ch08_layered/exercise/service"）
     * @param forbidden   import 文に含まれてはいけない文字列（例: ".exercise.presentation."）
     */
    public static void assertNoImport(String relativeDir, String forbidden) {
        for (Path file : javaFiles(relativeDir)) {
            try {
                for (String line : Files.readAllLines(file)) {
                    String t = line.trim();
                    assertFalse(t.startsWith("import ") && t.contains(forbidden),
                            relativeDir + " は " + forbidden + " に依存してはいけません: " + file.getFileName() + " の「" + t + "」");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * パッケージ内のどれかのファイルが、指定の import を含むことを確かめます（期待する依存の向きがあることの確認）。
     *
     * @param relativeDir 検査するパッケージ
     * @param expected    含まれていてほしい import の文字列
     */
    public static void assertSomeFileImports(String relativeDir, String expected) {
        boolean found = false;
        for (Path file : javaFiles(relativeDir)) {
            try {
                for (String line : Files.readAllLines(file)) {
                    String t = line.trim();
                    if (t.startsWith("import ") && t.contains(expected)) {
                        found = true;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        assertTrue(found, relativeDir + " のどれかのクラスが " + expected + " を使う（依存する）はずです");
    }

    /**
     * 検査対象のパッケージが、ファイルを1つ以上持つうえで、禁止された import を含まないことを確かめます。
     *
     * @param relativeDir 検査するパッケージ
     * @param forbidden   含まれてはいけない import の文字列
     */
    public static void assertExistsAndNoImport(String relativeDir, String forbidden) {
        assertPackageExists(relativeDir);
        assertNoImport(relativeDir, forbidden);
        assertTrue(true);
    }
}
