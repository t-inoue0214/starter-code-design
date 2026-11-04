package com.example.orders.ch09_domain_model.exercise;

import com.example.orders.domain.MemberRank;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderLine;
import com.example.orders.domain.OrderProcessor;
import com.example.orders.domain.OrderResult;
import com.example.orders.domain.OrderValidationException;
import com.example.orders.domain.OutOfStockException;
import com.example.orders.domain.PaymentDeclinedException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 第9章の解答例です。業務ルールを Money と Stock に持たせ、保存先を StockRepository に分けています。
 * このクラスは、手順をつなぐだけの薄い調整役になります。
 */
public class ExerciseOrderProcessor implements OrderProcessor {
    // 在庫の保存先を持つフィールド
    private final StockRepository repository;
    // 出力先を持つフィールド
    private final PrintStream out;

    /**
     * Map を保存先にした標準の構成で生成します。
     *
     * @param stock 商品IDごとの在庫数
     * @param out   出力先
     */
    public ExerciseOrderProcessor(Map<String, Integer> stock, PrintStream out) {
        // ★重要★ 標準の保存先を渡す（組み立て役はここ）
        this(new InMemoryStockRepository(stock), out);
    }

    /**
     * 在庫の保存先を外から受け取るコンストラクタです。
     *
     * @param repository 在庫の保存先
     * @param out        出力先
     */
    public ExerciseOrderProcessor(StockRepository repository, PrintStream out) {
        // 引数の在庫の保存先を、フィールドに保存する
        this.repository = repository;
        // 引数の出力先を、フィールドに保存する
        this.out = out;
    }

    /**
     * 注文を処理します。在庫の判断は Stock に、保存はリポジトリに任せます。
     *
     * @param order 注文
     * @return 金額の内訳
     */
    @Override
    public OrderResult process(Order order) {
        // 1. 入力検証
        if (order == null || order.customer() == null) {
            // 注文か注文者がないことを、検証エラーとして投げる
            throw new OrderValidationException("注文または注文者がありません");
        }
        // 明細がない注文は、処理できない
        if (order.lines() == null || order.lines().isEmpty()) {
            // 明細がないことを、検証エラーとして投げる
            throw new OrderValidationException("明細がありません");
        }
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 数量が0以下の明細は、不正として扱う
            if (line.quantity() <= 0) {
                // 数量が不正であることを、検証エラーとして投げる
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            // 単価が負の明細は、不正として扱う
            if (line.unitPrice() < 0) {
                // 単価が負であることを、検証エラーとして投げる
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }

        // 2. 金額計算（金額は Money で扱う）
        Money subtotalMoney = Money.zero();
        // 注文の明細を1行ずつ取り出して処理する
        for (OrderLine line : order.lines()) {
            // 単価 × 数量を、金額（Money）として小計に加える
            subtotalMoney = subtotalMoney.plus(Money.of(line.unitPrice()).times(line.quantity()));
        }
        // 割引額を、0円から数え始める
        Money discountMoney = Money.zero();
        // ゴールド会員なら
        if (order.customer().rank() == MemberRank.GOLD) {
            // 小計の10%を、割引額に加える
            discountMoney = discountMoney.plus(subtotalMoney.percent(10));
        // シルバー会員なら
        } else if (order.customer().rank() == MemberRank.SILVER) {
            // 小計の5%を、割引額に加える
            discountMoney = discountMoney.plus(subtotalMoney.percent(5));
        }
        // 注文に指定されたクーポンコードを取り出す
        String coupon = order.couponCode();
        // クーポンが指定されているときだけ
        if (coupon != null) {
            // WELCOME500 は、500円引きのクーポン
            if (coupon.equals("WELCOME500")) {
                // 500円を、割引額に加える
                discountMoney = discountMoney.plus(Money.of(500));
            // SAVE10 は、小計の10%引きのクーポン
            } else if (coupon.equals("SAVE10")) {
                // 小計の10%を、割引額に加える
                discountMoney = discountMoney.plus(subtotalMoney.percent(10));
            // それ以外のとき
            } else {
                // 登録されていないクーポンは、検証エラーとして投げる
                throw new OrderValidationException("未知のクーポンです: " + coupon);
            }
        }
        // 注文日が12月なら
        if (order.orderedOn().getMonthValue() == 12) {
            // 小計の3%を、季節割引として加える
            discountMoney = discountMoney.plus(subtotalMoney.percent(3));
        }
        // 割引は小計を超えない
        discountMoney = discountMoney.min(subtotalMoney);
        // 小計を、円の数値として取り出す
        int subtotal = subtotalMoney.yen();
        // 割引額を、円の数値として取り出す
        int discount = discountMoney.yen();
        // 請求額を、小計から割引額を引いて求める
        int total = subtotalMoney.minus(discountMoney).yen();

        // ★重要★ 3. 在庫引当（足りるかの判断は Stock が行う。途中で足りなければ戻す）
        List<OrderLine> reserved = new ArrayList<>();
        // 例外が起こりうる処理を、ここから試す
        try {
            // 注文の明細を1行ずつ取り出して処理する
            for (OrderLine line : order.lines()) {
                // 保存先から、その商品の在庫を取り出す
                Stock stock = repository.find(line.productId());
                // ★重要★ 「足りるか」の判断は Stock に任せる（足りなければ例外が出る）
                stock.reserve(line.quantity());
                // 変更した在庫を、保存先に書き戻す
                repository.save(stock);
                // 引き当てた明細を、戻せるように覚えておく
                reserved.add(line);
            }
        // 途中の明細で在庫が足りなかったとき
        } catch (OutOfStockException e) {
            // 引き当て済みの在庫を、すべて元に戻す
            releaseAll(reserved);
            // 在庫を戻したあとで、例外を呼び出し側へ投げ直す
            throw e;
        }

        // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
        if (Money.of(total).isGreaterThan(Money.of(500000))) {
            // 引き当て済みの在庫を、すべて元に戻す
            releaseAll(reserved);
            // 決済の上限を超えたので、決済拒否の例外を投げる
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知
        if (order.customer().email() != null) {
            // メール通知を出力する（宛先、注文ID、請求額）
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + total);
        }
        // 電話番号があり、請求額が1万円以上のときだけ
        if (order.customer().phone() != null && total >= 10000) {
            // SMS通知を出力する（宛先、注文ID）
            out.println("[SMS] to=" + order.customer().phone()
                    + " order=" + order.id());
        }

        // 6. 監査ログ
        out.println("[AUDIT] order=" + order.id()
                + " customer=" + order.customer().id()
                + " total=" + total);

        // 金額の内訳を呼び出し元へ返す
        return new OrderResult(order.id(), subtotal, discount, total);
    }

    /** 引き当て済みの在庫をすべて戻します。 */
    private void releaseAll(List<OrderLine> lines) {
        // 明細を1行ずつ取り出して処理する
        for (OrderLine line : lines) {
            // 保存先から、その商品の在庫を取り出す
            Stock stock = repository.find(line.productId());
            // ★重要★ 在庫を戻すルールは、Stock が持つ
            stock.release(line.quantity());
            // 変更した在庫を、保存先に書き戻す
            repository.save(stock);
        }
    }
}
