package net.sebyte.tasks

import kotlinx.cli.ArgType
import kotlinx.cli.default
import net.sebyte.*
import net.sebyte.ast.Select
import net.sebyte.cli.Logger
import net.sebyte.gen.OutputMap
import net.sebyte.gen.SelectGenerator
import net.sebyte.mut.Mutator
import kotlin.math.max

private val IGNORED_CODES = listOf(NOT_TERMINATED, 0, 1) // 0 = Success, 1 = Syntax Error

class CoverageTask : BasicTask(
    "coverage", "Test a single test subject and mutate queries based on coverage metrics."
) {
    val numberOfQueries by option(
        ArgType.Int, "queries", "n",
        "Initially generated queries"
    ).default(1000)

    private val mutations by option(
        ArgType.Int, "mutations", "m",
        "Mutations per successful query"
    ).default(100)

    private val coverageRegex = "^Lines executed:(\\d+\\.?\\d*)% of \\d+$".toRegex()
    private val codes = mutableMapOf<Int, Int>()

    private fun getCoverage(): Double {
        val (gcovCode, gcovOut, _) = runCmd(listOf("gcov", "-r", "./sqlite3-sqlite3"))
        check(gcovCode == 0)
        val line = gcovOut.trim().lines().last()
        val match = coverageRegex.find(line)
        val coverage = match?.groups[1]?.value?.toDoubleOrNull()
        checkNotNull(coverage) { "Line could not be parsed:\n$line" }
        return coverage
    }

    override fun run() {
        val dataSources = createDataSources(cfg, 10..20)
        val createSql = createDatabase(cfg, dataSources)

        runSql("./sqlite3", createSql, workDir = ".")
        val baseline = getCoverage()
        Logger.info { "Coverage baseline (no selects): $baseline" }
        runSql("./sqlite3", "select 1;", workDir = ".")
        var coverage = getCoverage()
        Logger.info { "Coverage (SELECT 1): $coverage" }

        val generator = SelectGenerator(cfg, dataSources)
        val queue = ArrayDeque<Pair<Select, OutputMap>>(10)
        for (i in 0..numberOfQueries) {
            val outMap: OutputMap = mutableMapOf()
            queue.addLast(generator.select(outMap) to outMap)
        }

        val mutator = Mutator(cfg, dataSources)
        while (queue.isNotEmpty()) {
            val (query, outMap) = queue.removeFirst()
            val (code, _, err) = runSql("./sqlite3", query.toString(), workDir = ".")
            codes[code] = codes.getOrDefault(code, 0) + 1

            if (code !in IGNORED_CODES) Logger.info {
                """
                Interesting return code found!
                Code: $code
                Err: $err
                Query:
                $query       
            """.trimIndent()
            }

            val cov = getCoverage()
            if (coverage < cov) {
                Logger.debug { "Increased coverage: $coverage < $cov" }
                coverage = cov
                for (i in 0..mutations) queue.addLast(mutator.mutate(query, outMap) to outMap)
            }
        }

        Logger.info { "Final coverage: $coverage" }
        Logger.info { "Error codes:" }
        for ((code, count) in codes) {
            val tag = when (code) {
                0 -> "success"
                1 -> "handled error"
                NOT_TERMINATED -> "not terminated"
                else -> "unknown"
            }
            Logger.info { "$code ($tag): $count" }
        }
    }
}