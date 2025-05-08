package net.sebyte.tasks

import kotlinx.cli.*
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.cfg.SQLITE_v3_39_4
import net.sebyte.cfg.SQLITE_v3_44_4
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator
import net.sebyte.runSql
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.IntStream
import kotlin.random.Random

enum class SQLiteConfig {
    v3_44_4,
    v3_39_4,
    v3_26_0
}

@OptIn(ExperimentalCli::class)
abstract class BasicTask(name: String, description: String) : Subcommand(name, description) {
    val seed by option(
        ArgType.Int, "seed", "s", description = "Seed for randomness for reproducibility"
    )

    private val _config by option(
        ArgType.Choice<SQLiteConfig>(), "config", "c", description = "Config file to write configuration"
    ).default(SQLiteConfig.v3_26_0)

    val cfg: GeneratorConfig
        get() = when (_config) {
            SQLiteConfig.v3_44_4 -> SQLITE_v3_44_4
            SQLiteConfig.v3_39_4 -> SQLITE_v3_39_4
            SQLiteConfig.v3_26_0 -> SQLITE_v3_44_4
        }
}

abstract class BasicQueryTask(name: String, description: String) : BasicTask(name, description) {
    val numberOfQueries by option(
        ArgType.Int, "number-of-queries", "n",
        "Number of queries to generate"
    ).default(100_000)
}

abstract class BasicTestTask(name: String, description: String) : BasicQueryTask(name, description) {
    val testPath by argument(
        ArgType.String, "testPath", "Path to subject under test"
    ).optional().default("/usr/bin/sqlite3-3.26.0")

    val bugDir by option(
        ArgType.String, "bug-dir", description = "Path to directory where bugs are stored"
    ).default(".docker/")

    lateinit var workDir: String
        private set

    override fun execute() {
        val id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        val dataSources = createDataSources(cfg, 10..20)
        val createSql = createDatabase(cfg, dataSources)

        workDir = "./test_$id"
        File(workDir).mkdirs()
        File("$workDir/create.sql").writeText(createSql)
        runSql(testPath, createSql, workDir = workDir)

        val selectGenerator = SelectGenerator(cfg, dataSources)
        val pbb = ProgressBarBuilder()
            .setTaskName("Testing")
            .setUnit("tests", 1L)
            .showSpeed()
        for (i in ProgressBar.wrap(IntStream.range(1, numberOfQueries + 1), pbb)) {
            val query = selectGenerator.select().toString()
            executeTest(query, i)
        }

    }

    abstract fun executeTest(query: String, caseNumber: Int)
}