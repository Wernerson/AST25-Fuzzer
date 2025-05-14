package net.sebyte.run

import net.sebyte.cfg.*
import net.sebyte.cli.Logger
import net.sebyte.cli.pbar
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import runSql
import java.io.File

class Trial(
    private val legislator: Legislator,
    private val executor: Executor,
    private val judicator: Judicator,
    private val clerk: Clerk,
    private val showProgressBar: Boolean = true
) {
    fun run() {
        val queries = if (showProgressBar) legislator.pbar() else legislator
        for (query in queries) {
            val result = executor.execute(query)
            val verdict = judicator.judge(query, result)
            clerk.report(query, result, verdict)
            legislator.notice(query, verdict)
        }
        clerk.summarise()
    }

    companion object {
        fun from(cfg: RunConfig): Trial {
            val genConfig = when (cfg.generator) {
                SQLiteConfig.v3_26_0 -> SQLITE_v3_26_0
                SQLiteConfig.v3_39_4 -> SQLITE_v3_39_4
                SQLiteConfig.v3_44_4 -> SQLITE_v3_44_4
            }

            Logger.debug { "Creating create.sql..." }
            val tables = createDataSources(genConfig, cfg.noTables, cfg.noColumns)
            val createSql = createDatabase(genConfig, tables)

            if (cfg.subject == null) {
                println(createSql)
                println()
                val legislator = SimpleLegislator(cfg.queries, genConfig, tables)
                return Trial(legislator, LogExecutor, IgnorantJudicator, IgnorantClerk, false)
            }

            val testDb = cfg.testDb?.let { File(it) }
            if (testDb != null) {
                testDb.parentFile.mkdirs()
                if (testDb.exists()) testDb.delete()
                testDb.createNewFile()
                Logger.debug { "Creating database file '$testDb'..." }
                runSql(cfg.subject, createSql, testDb, timeout = 30)
            }

            val legislator = if (cfg.mutations == null) SimpleLegislator(cfg.queries, genConfig, tables)
            else MutableLegislator(cfg.queries, cfg.mutations, genConfig, tables)

            val executor = if (testDb == null) InMemoryExecutor(cfg.subject, createSql)
            else TestDbExecutor(cfg.subject, testDb)

            var judicator = if (cfg.oracle == null) ErrorCodeJudicator
            else if (testDb != null) DifferentialJudicator(TestDbExecutor(cfg.oracle, testDb))
            else DifferentialJudicator(InMemoryExecutor(cfg.oracle, createSql))

            if (cfg.coverage) {
                val covPath = File(cfg.subject).parentFile.absolutePath
                judicator = CoverageJudicator(judicator, "$covPath/sqlite3-sqlite3")
            }

            val archiveDir = cfg.archiveDir?.let { File(it) }
            val clerk = if (cfg.coverage) {
                val covPath = File(cfg.subject).parentFile.absolutePath
                CoverageClerk("$covPath/sqlite3-sqlite3", archiveDir)
            } else BaseClerk(archiveDir)

            return Trial(legislator, executor, judicator, clerk)
        }
    }
}