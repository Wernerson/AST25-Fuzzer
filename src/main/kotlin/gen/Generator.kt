package net.sebyte.gen

import net.sebyte.cfg.GeneratorConfig
import kotlin.random.nextInt

abstract class Generator(
    val cfg: GeneratorConfig
) {
    val r = cfg.r
    fun nextBoolean(pct: Double) = r.nextDouble() < pct

    fun <T> oneOf(options: Collection<T>): T = options.random(r)
    fun <T> oneOf(vararg options: T): T = options.random(r)

    inline fun <T> oneOf(
        crossinline block: MutableList<() -> T>.() -> Unit
    ): T = buildList(block).random(r)()

    fun <T> listOf(range: IntRange, options: Collection<T>): List<T> =
        List(r.nextInt(range)) { options.random(r) }

    fun <T> listOf(range: IntRange, vararg options: T): List<T> =
        List(r.nextInt(range)) { options.random(r) }

    inline fun <T> listOf(
        range: IntRange, crossinline block: (Int) -> T
    ): List<T> = List(r.nextInt(range), block)

//    protected inline fun <T> listOf(
//        range: IntRange, crossinline block: MutableList<() -> T>.() -> Unit
//    ): List<T> = List(r.nextInt(range)) { buildList(block).random(r)() }

}