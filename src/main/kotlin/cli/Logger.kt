package net.sebyte.cli

import me.tongfei.progressbar.InteractiveConsoleProgressBarConsumer
import me.tongfei.progressbar.ProgressBarConsumer

object Logger {

    var verbose = false

    @Volatile
    private var printed = false

    fun debug(block: () -> Any) {
        if (verbose) {
            printed = true
            println(block())
        }
    }

    fun info(block: () -> Any) {
        printed = true
        println(block())
    }

    fun progress() = object : ProgressBarConsumer {
        val consumer = InteractiveConsoleProgressBarConsumer(System.out)

        override fun getMaxRenderedLength(): Int = consumer.getMaxRenderedLength()

        override fun accept(rendered: String) {
            if (printed) {
                println()
                printed = false
            }
            consumer.accept(rendered)
        }

        override fun close() {
            System.out.flush()
        }
    }
}