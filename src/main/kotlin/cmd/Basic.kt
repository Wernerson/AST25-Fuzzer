package net.sebyte.tasks

import kotlinx.cli.*
import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator
import net.sebyte.runSql
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.IntStream
import kotlin.random.Random

@OptIn(ExperimentalCli::class)
abstract class BasicTask(name: String, description: String) : Subcommand(name, description) {
    val seed by option(
        ArgType.Int, "seed", description = "Seed for randomness for reproducibility"
    )
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

    lateinit var rand: Random
        private set

    override fun execute() {
        val id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
        rand = seed?.let { Random(it) } ?: Random.Default
        val dataSources = createDataSources(rand, 10..20)
        val createSql = createDatabase(rand, dataSources)

        workDir = "./test_$id"
        File(workDir).mkdirs()
        File("$workDir/create.sql").writeText(createSql)
        runSql(testPath, createSql, workDir = workDir)

        val selectGenerator = SelectGenerator(rand, dataSources)
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