package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default
import net.sebyte.cfg.SQLITE_v3_26_0
import net.sebyte.cli.Logger
import net.sebyte.run.*
import java.io.File


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")

    val version by parser.option(ArgType.Boolean, "version", description = "Display version").default(false)
    val verbose by parser.option(ArgType.Boolean, "verbose", "v", "Display version").default(false)
    parser.parse(args)

    Logger.verbose = verbose
    if (version) {
        Logger.info { "v0.1.0 by Sebastian Brunner" }
        return
    }

    val preparator = TestDbPreparator(SQLITE_v3_26_0, 20, 5, "sqlite3-3.26.0", File("./test.db"))
//    val preparator = Preparator(SQLITE_v3_26_0, 20, 5)
    val env = preparator.prepare()
//    val legislator = MutableLegislator(100, 100, SQLITE_v3_26_0, env.tables)
    val legislator = SimpleLegislator(100, SQLITE_v3_26_0, env.tables)
    val executor = TestDbExecutor("./sqlite3", File("./test.db"))
    val oracleExecutor = TestDbExecutor("sqlite3-3.44.4", File("./test.db"))
//    val executor = LogExecutor
    val judicator = CoverageJudicator(DifferentialJudicator(oracleExecutor), "sqlite3-sqlite3")
    val clerk = CoverageClerk(execPath = "sqlite3-sqlite3")
    val trial = Trial(legislator, executor, judicator, clerk)
    trial.run()
}