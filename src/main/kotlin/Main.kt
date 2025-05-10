package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import net.sebyte.cli.Logger
import net.sebyte.cli.pbar
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

    val test = object: Subcommand("test", "") {
        override fun execute() {
            val rand = Random.Default
            for(i in (1..10).pbar("test")) {
                sleep(rand.nextLong(100, 1000))
                if (rand.nextBoolean()) Logger.info { "Count to $i" }
            }
        }
    }

    parser.subcommands(compare, crash, queries, coverage, test)
    parser.parse(args)
}