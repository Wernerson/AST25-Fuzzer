package net.sebyte.run

import net.sebyte.cfg.*
import net.sebyte.cli.pbar
import java.io.File

class Trial(
    private val legislator: Legislator,
    private val executor: Executor,
    private val judicator: Judicator,
    private val clerk: Clerk
) {
    fun run() {
        for (query in legislator.pbar()) {
            val result = executor.execute(query)
            val verdict = judicator.judge(query, result)
            clerk.report(query, result, verdict)
            legislator.notice(query, verdict)
        }
        clerk.summarise()
    }

    companion object {
        fun from(cfg: RunConfig): Trial {
            val genConfig = when (cfg.generatorConfig) {
                SQLiteConfig.v3_26_0 -> SQLITE_v3_26_0
                SQLiteConfig.v3_39_4 -> SQLITE_v3_39_4
                SQLiteConfig.v3_44_4 -> SQLITE_v3_44_4
            }

            val testDb = cfg.testDb?.let { File(it) }

            val preparator = if (testDb == null) Preparator(genConfig, cfg.noTables, cfg.noColumns)
            else TestDbPreparator(genConfig, cfg.noTables, cfg.noColumns, cfg.subject, testDb)
            val env = preparator.prepare()

            val legislator = if (cfg.mutations == null) SimpleLegislator(cfg.queries, genConfig, env.tables)
            else MutableLegislator(cfg.queries, cfg.mutations, genConfig, env.tables)

            val executor = if (testDb == null) InMemoryExecutor(cfg.subject, env.createSql)
            else TestDbExecutor(cfg.subject, testDb)

            var judicator = if (cfg.oracle == null) ErrorCodeJudicator
            else if (testDb != null) DifferentialJudicator(TestDbExecutor(cfg.oracle, testDb))
            else DifferentialJudicator(InMemoryExecutor(cfg.oracle, env.createSql))

            if (cfg.coverage) {
                judicator = CoverageJudicator(judicator, "sqlite3-sqlite3")
            }

            val clerk = if (cfg.coverage) CoverageClerk("sqlite3-sqlite3") else SummaryClerk()

            return Trial(legislator, executor, judicator, clerk)
        }
    }
}