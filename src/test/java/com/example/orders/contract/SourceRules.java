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
    // BASEを持つフィールド
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
        // パッケージ名を、ディレクトリの場所に直す
        Path dir = Path.of(BASE + relativeDir);
        // ディレクトリがないとき
        if (!Files.isDirectory(dir)) {
            // ディレクトリがなければ、空の一覧を返す
            return List.of();
        }
        // ディレクトリ直下のファイルを一覧にする（使い終わったら閉じる）
        try (Stream<Path> s = Files.list(dir)) {
            // Javaファイルだけを、名前順に集めて返す
            return s.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        // ファイルを読めなかったとき
        } catch (IOException e) {
            // ファイルの読み込みエラーを、検査なしの例外に包んで投げ直す
            throw new UncheckedIOException(e);
        }
    }

    /**
     * パッケージに、Java ファイルが1つ以上あることを確かめます。
     *
     * @param relativeDir 例: "ch04_layered/exercise/service"
     */
    public static void assertPackageExists(String relativeDir) {
        // 条件が成り立たないことを確かめる
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
        // パッケージ内のJavaファイルを1つずつ調べる
        for (Path file : javaFiles(relativeDir)) {
            // 例外が起こりうる処理を、ここから試す
            try {
                // ファイルの行を1行ずつ読む
                for (String line : Files.readAllLines(file)) {
                    // 行の前後の空白を取り除く
                    String t = line.trim();
                    // 条件が成り立たないことを確かめる
                    assertFalse(t.startsWith("import ") && t.contains(forbidden),
                            relativeDir + " は " + forbidden + " に依存してはいけません: " + file.getFileName() + " の「" + t + "」");
                }
            // ファイルを読めなかったとき
            } catch (IOException e) {
                // ファイルの読み込みエラーを、検査なしの例外に包んで投げ直す
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
        // まだ見つかっていない状態から始める
        boolean found = false;
        // パッケージ内のJavaファイルを1つずつ調べる
        for (Path file : javaFiles(relativeDir)) {
            // 例外が起こりうる処理を、ここから試す
            try {
                // ファイルの行を1行ずつ読む
                for (String line : Files.readAllLines(file)) {
                    // 行の前後の空白を取り除く
                    String t = line.trim();
                    // 期待する import が見つかったとき
                    if (t.startsWith("import ") && t.contains(expected)) {
                        // 見つかったことを記録する
                        found = true;
                    }
                }
            // ファイルを読めなかったとき
            } catch (IOException e) {
                // ファイルの読み込みエラーを、検査なしの例外に包んで投げ直す
                throw new UncheckedIOException(e);
            }
        }
        // 条件が成り立つことを確かめる
        assertTrue(found, relativeDir + " のどれかのクラスが " + expected + " を使う（依存する）はずです");
    }

    /**
     * 検査対象のパッケージが、ファイルを1つ以上持つうえで、禁止された import を含まないことを確かめます。
     *
     * @param relativeDir 検査するパッケージ
     * @param forbidden   含まれてはいけない import の文字列
     */
    public static void assertExistsAndNoImport(String relativeDir, String forbidden) {
        // パッケージにクラスがあることを確かめる
        assertPackageExists(relativeDir);
        // 禁止された import がないことを確かめる
        assertNoImport(relativeDir, forbidden);
        // 条件が成り立つことを確かめる
        assertTrue(true);
    }

    /**
     * パッケージに、名前が指定の語尾で終わるクラス（ファイル）が1つ以上あることを確かめます。
     *
     * @param relativeDir 検査するパッケージ
     * @param suffix      クラス名の語尾（例: "Controller"）
     */
    public static void assertClassWithSuffixExists(String relativeDir, String suffix) {
        // 指定の語尾のクラスが、1つでもあるかを調べる
        boolean found = javaFiles(relativeDir).stream()
                .anyMatch(p -> p.getFileName().toString().endsWith(suffix + ".java"));
        // 条件が成り立つことを確かめる
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
        // パッケージ内のJavaファイルを1つずつ調べる
        for (Path file : javaFiles(relativeDir)) {
            // ファイル名が指定の語尾でないとき
            if (!file.getFileName().toString().endsWith(suffix + ".java")) {
                // このファイルは対象外なので、次へ進む
                continue;
            }
            // 例外が起こりうる処理を、ここから試す
            try {
                // ファイルの全文を読み込む
                String body = Files.readString(file);
                // 条件が成り立たないことを確かめる
                assertFalse(body.contains(forbidden),
                        file.getFileName() + " に「" + forbidden + "」があります。" + reason);
            // ファイルを読めなかったとき
            } catch (IOException e) {
                // ファイルの読み込みエラーを、検査なしの例外に包んで投げ直す
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
        // 語尾の指定なしで、全クラスを検査する
        assertNoTextInClasses(relativeDir, "", forbidden, reason);
    }

    /**
     * メソッドの行数を数えます。宣言の行から、同じインデントの閉じ括弧の行までです。
     * コメントの行と空行は数えません。
     *
     * @param relativeFile ソースファイル（例: "ch03_transaction_script/exercise/ExerciseOrderProcessor.java"）
     * @param signature    メソッド宣言の行に含まれる文字列
     * @return メソッドの行数（宣言行と閉じ括弧を含む）
     */
    public static int methodLines(String relativeFile, String signature) {
        // 例外が起こりうる処理を、ここから試す
        try {
            // ソースファイルの全行を読み込む
            List<String> lines = Files.readAllLines(Path.of(BASE + relativeFile));
            // ファイルの行を、上から順に調べる
            for (int i = 0; i < lines.size(); i++) {
                // 宣言の行を見つけたとき
                if (lines.get(i).contains(signature)) {
                    // 宣言の行のインデント（行頭の空白）を取り出す
                    String indent = lines.get(i).substring(0, lines.get(i).indexOf(lines.get(i).trim()));
                    // 宣言の行を、1行目として数え始める
                    int count = 1;
                    // 宣言の次の行から、閉じ括弧を探す
                    for (int j = i + 1; j < lines.size(); j++) {
                        // 行の前後の空白を取り除く
                        String body = lines.get(j).trim();
                        // 宣言と同じインデントの閉じ括弧に着いたとき
                        if (lines.get(j).equals(indent + "}")) {
                            // 閉じ括弧の行を足した、行数を返す
                            return count + 1;
                        }
                        // コメントと空行は数えない（コメントを増やしても、行数の検査に影響しないようにする）
                        if (!body.isEmpty() && !body.startsWith("//") && !body.startsWith("*") && !body.startsWith("/*")) {
                            // コード行を1行数える
                            count++;
                        }
                    }
                }
            }
            // 宣言が見つからなかったので、例外を投げる
            throw new IllegalStateException("メソッドが見つかりません: " + signature);
        // ファイルを読めなかったとき
        } catch (IOException e) {
            // ファイルの読み込みエラーを、検査なしの例外に包んで投げ直す
            throw new UncheckedIOException(e);
        }
    }
}
