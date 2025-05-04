package net.sebyte.gen

import kotlin.random.Random
import kotlin.random.nextInt

abstract class Generator(
    protected val r: Random
) {
    protected fun nextBoolean(pct: Double) = r.nextDouble() < pct

    protected fun <T> oneOf(options: Collection<T>): T = options.random(r)
    protected fun <T> oneOf(vararg options: T): T = options.random(r)

    protected inline fun <T> oneOf(
        crossinline block: MutableList<() -> T>.() -> Unit
    ): T = buildList(block).random(r)()

    protected fun <T> listOf(range: IntRange, options: Collection<T>): List<T> =
        List(r.nextInt(range)) { options.random(r) }

    protected fun <T> listOf(range: IntRange, vararg options: T): List<T> =
        List(r.nextInt(range)) { options.random(r) }

    protected inline fun <T> listOf(
        range: IntRange, crossinline block: (Int) -> T
    ): List<T> = List(r.nextInt(range), block)

//    protected inline fun <T> listOf(
//        range: IntRange, crossinline block: MutableList<() -> T>.() -> Unit
//    ): List<T> = List(r.nextInt(range)) { buildList(block).random(r)() }

}