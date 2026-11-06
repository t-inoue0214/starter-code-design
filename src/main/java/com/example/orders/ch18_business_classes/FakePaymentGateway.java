package com.example.orders.ch18_business_classes;

import com.example.orders.domain.PaymentDeclinedException;
import java.util.ArrayList;
import java.util.List;

/**
 * 決済ゲートウェイの、動作確認用の実装です。実際には通信せず、請求の記録だけを残します。
 * 外部サービスの代わりに差し替えられるので、テストで拒否も再現できます。
 */
public class FakePaymentGateway implements PaymentGateway {
    // 拒否するかどうかを持つフィールド
    private final boolean decline;
    // 請求された金額の記録を持つフィールド
    private final List<Integer> charged = new ArrayList<>();

    /**
     * ゲートウェイを作ります。
     *
     * @param decline true なら、すべての請求を拒否する
     */
    public FakePaymentGateway(boolean decline) {
        // 引数の拒否設定を、フィールドに保存する
        this.decline = decline;
    }

    /**
     * 決済を請求します。拒否の設定のときは、例外を投げます。
     *
     * @param customerId 請求する顧客のID
     * @param amount     請求額（円）
     */
    @Override
    public void charge(String customerId, int amount) {
        // 拒否の設定のとき
        if (decline) {
            // ★重要★ 外部サービスの拒否は、業務の例外に変えて、業務ロジックへ返す
            throw new PaymentDeclinedException("決済が拒否されました: " + customerId);
        }
        // 請求額を、記録する
        charged.add(amount);
    }

    /** @return 請求された金額の記録 */
    public List<Integer> charged() { return List.copyOf(charged); }
}
