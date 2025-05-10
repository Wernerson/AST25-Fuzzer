package net.sebyte.cli

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import java.util.stream.IntStream
import java.util.stream.Stream

fun IntRange.pbar(name: String): Stream<Int> {
    val pbb = ProgressBarBuilder()
        .setTaskName(name)
        .setUnit("queries", 1L)
        .showSpeed()
    return ProgressBar.wrap(IntStream.range(start, endInclusive), pbb)
}