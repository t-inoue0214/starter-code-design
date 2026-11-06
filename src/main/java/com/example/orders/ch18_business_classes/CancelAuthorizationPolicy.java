package com.example.orders.ch18_business_classes;

/**
 * 認可ポリシーの例です。「この人が、この注文をキャンセルしてよいか」だけを判定します。
 * 権限ごとのロジックは、新しい種類ではなく、ポリシーの一つとして書きます。
 */
public class CancelAuthorizationPolicy {

    /**
     * キャンセルの権限があるかを判定します。注文の状態（キャンセルできる段階か）は見ません。
     *
     * @param order       対象の注文
     * @param requesterId 操作する人のID
     * @param role        操作する人の役割
     * @return 権限があれば許可、なければ理由付きの不許可
     */
    public PolicyDecision check(PlacedOrder order, String requesterId, Role role) {
        // 運用担当者のとき
        if (role == Role.ADMIN) {
            // 運用担当者は、どの注文でも許可する
            return PolicyDecision.allow();
        }
        // 注文した本人のとき
        if (order.customerId().equals(requesterId)) {
            // ★重要★ 権限（誰がやってよいか）だけを見る。状態のルールは注文自身が持つ
            return PolicyDecision.allow();
        }
        // 他人の注文なので、理由付きの不許可を返す
        return PolicyDecision.deny("他人の注文はキャンセルできません: " + order.id());
    }
}
