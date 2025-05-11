package net.sebyte.cli

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import net.sebyte.ast.Select
import net.sebyte.run.Legislator

fun Legislator.pbar(): Iterator<Select> {
    val pbb = ProgressBarBuilder()
        .setTaskName("Running tests")
        .setUnit("queries", 1L)
        .setConsumer(Logger.progress())
        .showSpeed()
    size?.let { pbb.setInitialMax(it.toLong()) }
    return ProgressBar.wrap(this, pbb)
}