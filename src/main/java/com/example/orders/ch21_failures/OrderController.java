package com.example.orders.ch21_failures;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 入口の部品です。結果と例外を、利用者向けの文章に変えます。
 * 利用者向けの言葉に変えるのは、この入口だけです。サービスは、失敗の種類だけを返します。
 */
public final class OrderController {
    // 注文のサービスを持つフィールド
    private final OrderService service;

    /**
     * 入口を作ります。
     *
     * @param service 注文のサービス
     */
    public OrderController(OrderService service) {
        // 引数のサービスを、フィールドに保存する
        this.service = service;
    }

    /**
     * 注文の入力を受け取り、利用者に返す文章を作ります。
     *
     * @param request 注文の入力
     * @return 利用者に返す文章
     */
    public String handle(OrderRequest request) {
        // ★重要★ 入力そのものがないのは、呼び出し側のプログラムの誤り。隠さず、すぐに例外で止める
        Objects.requireNonNull(request, "request");
        // 例外が起こりうる処理を、ここから試す
        try {
            // サービスに注文を任せる
            PlaceOrderResult result = service.place(request);
            // 入力の誤りのとき
            if (result.isInvalid()) {
                // 誤りの説明を、すべてつないで返す
                return result.errors().stream().map(FieldError::message).collect(Collectors.joining("。"));
            }
            // 業務の失敗のとき
            if (!result.isSuccess()) {
                // 失敗の種類を、利用者向けの説明に変えて返す
                return messageOf(result.failure());
            }
            // 成功の文章を返す
            return "注文を受け付けました（" + result.orderId() + "、" + result.total() + "円）";
        // 決済を一時的に使えないとき
        } catch (PaymentUnavailableException e) {
            // ★重要★ 捕まえるのは、意味の分かる例外だけ。プログラムの誤りまで、まとめて捕まえない
            return "ただいま決済を利用できません。時間をおいて、もう一度お試しください";
        }
    }

    /**
     * 業務の失敗を、利用者向けの説明に変えます。
     *
     * @param failure 失敗の種類
     * @return 利用者向けの説明
     */
    static String messageOf(OrderFailure failure) {
        // ★重要★ 画面の言葉は入口で決める。サービスは失敗の種類だけを返し、文章を知らない
        switch (failure) {
            // 商品が見つからないとき
            case UNKNOWN_PRODUCT:
                // 商品が見つからないことを伝える
                return "指定の商品が見つかりません";
            // 在庫が足りないとき
            case OUT_OF_STOCK:
                // 数量を減らすよう促す
                return "在庫が足りません。数量を減らしてください";
            // 上限金額を超えたとき
            case OVER_LIMIT:
                // 上限を超えたことを伝える
                return "1回の注文の上限金額を超えています";
            // カードが断られたとき
            case PAYMENT_DECLINED:
                // 別のカードを促す
                return "カードが使えませんでした。別のカードをお試しください";
            // それ以外の種類のとき
            default:
                // 種類を足したのに、ここを直し忘れたプログラムの誤りとして止める
                throw new IllegalArgumentException("説明のない失敗です: " + failure);
        }
    }
}
