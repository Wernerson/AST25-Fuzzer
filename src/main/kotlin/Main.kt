package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default
import net.sebyte.tasks.CompareTask
import net.sebyte.tasks.CoverageTask
import net.sebyte.tasks.CrashTask
import net.sebyte.tasks.QueriesTask


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")
    val version by parser.option(ArgType.Boolean, "version", description = "Prints version").default(false)
    val compare = CompareTask()
    val crash = CrashTask()
    val queries = QueriesTask()
    val coverage = CoverageTask()
    parser.subcommands(compare, crash, queries, coverage)
    parser.parse(args)
    if (version) println("v0.1.0 by Sebastian Brunner")
}