package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import net.sebyte.tasks.CompareCommand
import net.sebyte.tasks.CrashCommand
import net.sebyte.tasks.QueriesTask


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")
    val compare = CompareCommand()
    val crash = CrashCommand()
    val queries = QueriesTask()
    parser.subcommands(compare, crash, queries)
    parser.parse(args)
}