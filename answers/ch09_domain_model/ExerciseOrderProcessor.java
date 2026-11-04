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
    private final StockRepository repository;
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
        this.repository = repository;
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
            throw new OrderValidationException("注文または注文者がありません");
        }
        if (order.lines() == null || order.lines().isEmpty()) {
            throw new OrderValidationException("明細がありません");
        }
        for (OrderLine line : order.lines()) {
            if (line.quantity() <= 0) {
                throw new OrderValidationException("数量は1以上にしてください: " + line.productId());
            }
            if (line.unitPrice() < 0) {
                throw new OrderValidationException("単価が負です: " + line.productId());
            }
        }

        // 2. 金額計算（金額は Money で扱う）
        Money subtotalMoney = Money.zero();
        for (OrderLine line : order.lines()) {
            subtotalMoney = subtotalMoney.plus(Money.of(line.unitPrice()).times(line.quantity()));
        }
        Money discountMoney = Money.zero();
        if (order.customer().rank() == MemberRank.GOLD) {
            discountMoney = discountMoney.plus(subtotalMoney.percent(10));
        } else if (order.customer().rank() == MemberRank.SILVER) {
            discountMoney = discountMoney.plus(subtotalMoney.percent(5));
        }
        String coupon = order.couponCode();
        if (coupon != null) {
            if (coupon.equals("WELCOME500")) {
                discountMoney = discountMoney.plus(Money.of(500));
            } else if (coupon.equals("SAVE10")) {
                discountMoney = discountMoney.plus(subtotalMoney.percent(10));
            } else {
                throw new OrderValidationException("未知のクーポンです: " + coupon);
            }
        }
        if (order.orderedOn().getMonthValue() == 12) {
            discountMoney = discountMoney.plus(subtotalMoney.percent(3));
        }
        // 割引は小計を超えない
        discountMoney = discountMoney.min(subtotalMoney);
        int subtotal = subtotalMoney.yen();
        int discount = discountMoney.yen();
        int total = subtotalMoney.minus(discountMoney).yen();

        // ★重要★ 3. 在庫引当（足りるかの判断は Stock が行う。途中で足りなければ戻す）
        List<OrderLine> reserved = new ArrayList<>();
        try {
            for (OrderLine line : order.lines()) {
                Stock stock = repository.find(line.productId());
                stock.reserve(line.quantity());
                repository.save(stock);
                reserved.add(line);
            }
        } catch (OutOfStockException e) {
            releaseAll(reserved);
            throw e;
        }

        // 4. 決済（50万円を超える注文は拒否。拒否されたら在庫を戻す）
        if (Money.of(total).isGreaterThan(Money.of(500000))) {
            releaseAll(reserved);
            throw new PaymentDeclinedException("決済が拒否されました: " + order.id());
        }
        // 決済の記録を出力する（実際の決済サービスの代わり）
        out.println("[PAYMENT] order=" + order.id() + " amount=" + total);

        // 5. 通知
        if (order.customer().email() != null) {
            out.println("[MAIL] to=" + order.customer().email()
                    + " order=" + order.id() + " total=" + total);
        }
        if (order.customer().phone() != null && total >= 10000) {
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
        for (OrderLine line : lines) {
            Stock stock = repository.find(line.productId());
            stock.release(line.quantity());
            repository.save(stock);
        }
    }
}
