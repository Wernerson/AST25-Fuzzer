package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import net.sebyte.cli.Logger
import net.sebyte.tasks.BasicTestTask
import net.sebyte.tasks.CompareTask
import net.sebyte.tasks.CoverageTask
import net.sebyte.tasks.CrashTask
import net.sebyte.tasks.QueriesTask
import java.lang.Thread.sleep
import kotlin.random.Random
import kotlin.random.nextLong


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")
    val compare = CompareTask()
    val crash = CrashTask()
    val queries = QueriesTask()
    val coverage = CoverageTask()
    parser.subcommands(compare, crash, queries, coverage)
    parser.parse(args)
}