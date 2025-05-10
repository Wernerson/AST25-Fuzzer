package net.sebyte.cli

object Logger {

    var verbose = false

    fun debug(block: () -> Any) {
        if (verbose) println(block())
    }

    fun info(block: () -> Any) {
        println(block())
    }
}