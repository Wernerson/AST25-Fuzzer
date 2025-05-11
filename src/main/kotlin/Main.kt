package net.sebyte

import kotlinx.cli.ExperimentalCli
import net.sebyte.cfg.SQLITE_v3_26_0
import net.sebyte.run.*
import java.io.File


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
//    val parser = ArgParser("test-db")
//    val compare = CompareTask()
//    val crash = CrashTask()
//    val queries = QueriesTask()
//    val coverage = CoverageTask()
//    parser.subcommands(compare, crash, queries, coverage)
//    parser.parse(args)

    val preparator = TestDbPreparator(SQLITE_v3_26_0, 20, 5, "sqlite3-3.26.0", File("."))
//    val preparator = Preparator(SQLITE_v3_26_0, 20, 5)
    val env = preparator.prepare()
//    val legislator = MutableLegislator(100, 100, SQLITE_v3_26_0, env.tables)
    val legislator = SimpleLegislator(100, SQLITE_v3_26_0, env.tables)
    val executor = CoverageExecutor(TestDbExecutor("./sqlite3", File(".")), "sqlite3-sqlite3")
//    val executor = LogExecutor
    val judicator = CoverageJudicator(ComparisonJudicator)
    val clerk = SummaryClerk()
    val trial = Trial(legislator, executor, judicator, clerk)
    trial.run()
}