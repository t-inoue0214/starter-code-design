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

    /** 道具クラスなので、インスタンスは作りません。 */
    private SourceRules() {
    }

    /**
     * パッケージ（ディレクトリ）の直下にある Java ファイルを集めます。
     *
     * @param relativeDir 例: "ch04_layered/exercise/service"
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
     * @param relativeDir 例: "ch04_layered/exercise/service"
     */
    public static void assertPackageExists(String relativeDir) {
        assertFalse(javaFiles(relativeDir).isEmpty(),
                "パッケージが空です（クラスを作ってください）: " + relativeDir);
    }

    /**
     * パッケージ内のどのファイルも、禁止された import を含まないことを確かめます。
     *
     * @param relativeDir 検査するパッケージ（例: "ch04_layered/exercise/service"）
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

    /**
     * パッケージに、名前が指定の語尾で終わるクラス（ファイル）が1つ以上あることを確かめます。
     *
     * @param relativeDir 検査するパッケージ
     * @param suffix      クラス名の語尾（例: "Controller"）
     */
    public static void assertClassWithSuffixExists(String relativeDir, String suffix) {
        boolean found = javaFiles(relativeDir).stream()
                .anyMatch(p -> p.getFileName().toString().endsWith(suffix + ".java"));
        assertTrue(found, relativeDir + " に、名前が " + suffix + " で終わるクラスを作ってください");
    }

    /**
     * 名前が指定の語尾で終わるクラスが、禁止された文字列を含まないことを確かめます。
     * 役割ごとに「持ってはいけないもの」を確かめるために使います。
     *
     * @param relativeDir 検査するパッケージ
     * @param suffix      クラス名の語尾（例: "Service"）
     * @param forbidden   ファイルに含まれてはいけない文字列
     * @param reason      禁止する理由（失敗メッセージに使う）
     */
    public static void assertNoTextInClasses(String relativeDir, String suffix, String forbidden, String reason) {
        for (Path file : javaFiles(relativeDir)) {
            if (!file.getFileName().toString().endsWith(suffix + ".java")) {
                continue;
            }
            try {
                String body = Files.readString(file);
                assertFalse(body.contains(forbidden),
                        file.getFileName() + " に「" + forbidden + "」があります。" + reason);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * パッケージ内のどのファイルも、指定の文字列を含まないことを確かめます。
     *
     * @param relativeDir 検査するパッケージ
     * @param forbidden   含まれてはいけない文字列
     * @param reason      禁止する理由
     */
    public static void assertNoTextInPackage(String relativeDir, String forbidden, String reason) {
        assertNoTextInClasses(relativeDir, "", forbidden, reason);
    }

    /**
     * メソッドの行数を数えます。宣言の行から、同じインデントの閉じ括弧の行までです。
     *
     * @param relativeFile ソースファイル（例: "ch03_transaction_script/exercise/ExerciseOrderProcessor.java"）
     * @param signature    メソッド宣言の行に含まれる文字列
     * @return メソッドの行数（宣言行と閉じ括弧を含む）
     */
    public static int methodLines(String relativeFile, String signature) {
        try {
            List<String> lines = Files.readAllLines(Path.of(BASE + relativeFile));
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains(signature)) {
                    String indent = lines.get(i).substring(0, lines.get(i).indexOf(lines.get(i).trim()));
                    for (int j = i + 1; j < lines.size(); j++) {
                        if (lines.get(j).equals(indent + "}")) {
                            return j - i + 1;
                        }
                    }
                }
            }
            throw new IllegalStateException("メソッドが見つかりません: " + signature);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
