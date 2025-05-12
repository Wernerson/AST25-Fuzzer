package net.sebyte.run

import ExecResult
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.cli.Logger
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.Tables
import runSql
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Workspace(
    val tables: Tables,
    val createSql: String
)

open class Preparator(
    protected val generatorConfig: GeneratorConfig,
    protected val noTables: Int,
    protected val noColumns: Int
) {
    open fun prepare(): Workspace {
        val tables = createDataSources(generatorConfig, noTables, noColumns)
        val createSql = createDatabase(generatorConfig, tables)
        File("./create.sql").writeText(createSql)
        return Workspace(tables, createSql)
    }
}

class TestDbPreparator(
    generatorConfig: GeneratorConfig,
    noTables: Int,
    noColumns: Int,
    val subject: String,
    testDb: File? = null
) : Preparator(generatorConfig, noTables, noColumns) {

    val testDb = testDb ?: LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        .let { File("./test_db_$it/test.db") }

    override fun prepare(): Workspace {
        val workspace = super.prepare()
        testDb.parentFile.mkdirs()
        testDb.createNewFile()
        val result = runSql(subject, workspace.createSql, testDb, timeout = 30)

        when (result) {
            is ExecResult.Error -> Logger.debug { "Database setup had errors:\n${result.error}" }
            is ExecResult.Success -> Logger.debug { "Database setup without errors." }
            ExecResult.Timeout -> Logger.debug { "Database setup timed out." }
        }
        return workspace
    }
}