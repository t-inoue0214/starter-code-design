/**
 * 注文のパッケージです。外に公開するのは Order、OrderLine、OrderStatus の3つだけです。
 * 行の作り方と金額の計算は、このパッケージの中だけの事情なので、package-private にしています。
 * 外のパッケージからは、Order の public メソッドを通してだけ、注文を変えられます。
 */
package com.example.orders.ch20_visibility.order;
