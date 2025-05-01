package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("fuzzer")
    val compare = CompareTask()
    val crash = CrashTask()
    parser.subcommands(compare, crash)
    parser.parse(args)
}