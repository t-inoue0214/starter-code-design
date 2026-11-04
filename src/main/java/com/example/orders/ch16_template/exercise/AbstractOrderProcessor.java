package com.example.orders.ch16_template.exercise;

import com.example.orders.domain.OrderProcessor;

/**
 * 注文処理の骨格（手順）を定義する抽象クラスです。
 * TODO: 次の形で書いてください（テストがこの形を前提にしています）。
 *   - public final OrderResult process(Order order) をここに定義する（手順を固定する）
 *   - WebOrderProcessor と StoreOrderProcessor は、このクラスを継承する
 *   - 両方の子クラスから process メソッドの定義を取り除く
 *   - 子クラスに残すのは、チャネルごとに違う部分だけ
 */
public abstract class AbstractOrderProcessor implements OrderProcessor {
}
