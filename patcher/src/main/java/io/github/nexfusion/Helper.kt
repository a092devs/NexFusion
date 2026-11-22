package io.github.nexfusion

import io.github.a092devs.nexfusion.bindProxy as modernBindProxy

fun <T> Array<T>.atLast(index: Int) = this[this.size - index]

fun <TImpl : Any> TImpl.bindProxy(proxy: Any) {
    this.modernBindProxy(proxy)
}
