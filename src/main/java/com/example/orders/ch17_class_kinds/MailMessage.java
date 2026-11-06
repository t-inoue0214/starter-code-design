package com.example.orders.ch17_class_kinds;

import com.example.orders.domain.OrderValidationException;
import java.util.ArrayList;
import java.util.List;

/**
 * ビルダーの例です。項目が多く、省略できる項目もあるオブジェクトを、段階的に組み立てます。
 * 引数の多いコンストラクタを避け、どの値がどの項目かを、呼び出し側のコードで読めるようにします。
 */
public final class MailMessage {
    // 宛先を持つフィールド
    private final String to;
    // 件名を持つフィールド
    private final String subject;
    // 本文を持つフィールド
    private final String body;
    // CCの一覧を持つフィールド
    private final List<String> cc;

    /**
     * ビルダーに設定された値から作ります。外からは、ビルダー経由で作ります。
     *
     * @param builder 値を設定し終えたビルダー
     */
    private MailMessage(Builder builder) {
        // ビルダーに設定された宛先を、保存する
        this.to = builder.to;
        // ビルダーに設定された件名を、保存する
        this.subject = builder.subject;
        // ビルダーに設定された本文を、保存する
        this.body = builder.body;
        // CC を、書き換えられないコピーとして保存する
        this.cc = List.copyOf(builder.cc);
    }

    /** @return 組み立てを始めるためのビルダー */
    public static Builder builder() {
        // 空のビルダーを作って返す
        return new Builder();
    }

    /** @return 宛先 */
    public String to() { return to; }

    /** @return 件名 */
    public String subject() { return subject; }

    /** @return 本文 */
    public String body() { return body; }

    /** @return CC の一覧 */
    public List<String> cc() { return cc; }

    /** メールを組み立てるビルダーです。 */
    public static final class Builder {
        // 宛先を持つフィールド
        private String to;
        // 件名を持つフィールド
        private String subject = "";
        // 本文を持つフィールド
        private String body = "";
        // CCの一覧を持つフィールド
        private final List<String> cc = new ArrayList<>();

        /**
         * 宛先を設定します。
         *
         * @param to 宛先
         * @return このビルダー
         */
        public Builder to(String to) {
            // 引数の宛先を、フィールドに保存する
            this.to = to;
            // ★重要★ 自分自身を返して、呼び出しを . でつなげられるようにする
            return this;
        }

        /**
         * 件名を設定します。
         *
         * @param subject 件名
         * @return このビルダー
         */
        public Builder subject(String subject) {
            // 引数の件名を、フィールドに保存する
            this.subject = subject;
            // ★重要★ 自分自身を返して、呼び出しを . でつなげられるようにする
            return this;
        }

        /**
         * 本文を設定します。
         *
         * @param body 本文
         * @return このビルダー
         */
        public Builder body(String body) {
            // 引数の本文を、フィールドに保存する
            this.body = body;
            // ★重要★ 自分自身を返して、呼び出しを . でつなげられるようにする
            return this;
        }

        /**
         * CC を1件追加します。
         *
         * @param address 追加する CC
         * @return このビルダー
         */
        public Builder cc(String address) {
            // CC の一覧に、1件追加する
            this.cc.add(address);
            // ★重要★ 自分自身を返して、呼び出しを . でつなげられるようにする
            return this;
        }

        /**
         * 設定した値から、メールを作ります。宛先がなければ作れません。
         *
         * @return メール
         */
        public MailMessage build() {
            // ★重要★ 必須の宛先が、設定されていないとき（必須項目の検査は build で行う）
            if (to == null || to.isBlank()) {
                // 宛先がなければ、メールを作れない
                throw new OrderValidationException("宛先は必須です");
            }
            // 設定された値から、不変のメールを作って返す
            return new MailMessage(this);
        }
    }
}
