package net.sebyte.tasks

import net.sebyte.*
import net.sebyte.ast.Select
import net.sebyte.gen.SelectGenerator
import kotlin.math.max
import kotlin.random.Random

private val IGNORED_CODES = listOf(NOT_TERMINATED, 0, 1) // 0 = Success, 1 = Syntax Error

class CoverageTask : BasicTask(
    "coverage", "Test a single test subject and mutate queries based on coverage metrics."
) {
    private val coverageRegex = "^Lines executed:(\\d+\\.?\\d*)% of \\d+$".toRegex()

    private fun getCoverage(): Double {
        val (gcovCode, gcovOut, _) = runCmd(listOf("gcov", "-r", "./sqlite3-sqlite3"))
        check(gcovCode == 0)
        val line = gcovOut.trim().lines().last()
        val match = coverageRegex.find(line)
        val coverage = match?.groups[1]?.value?.toDoubleOrNull()
        checkNotNull(coverage) { "Line could not be parsed:\n$line" }
        return coverage
    }

    override fun execute() {
        val rand = seed?.let { Random(it) } ?: Random.Default
//        val dataSources = createDataSources(rand, 10..20)
//        val createSql = createDatabase(rand, dataSources)
//
//        runSql("./sqlite3", createSql, workDir = ".")
//        val baseline = getCoverage()
//        println("Coverage baseline (no selects): $baseline")
//        runSql("./sqlite3", "select 1;", workDir = ".")
//        var coverage = getCoverage()
//        println("Coverage (SELECT 1): $coverage")

//        val generator = SelectGenerator(rand, dataSources)
//        val queue = ArrayDeque<Select>(10)
//        for (i in 0..10) queue.addLast(generator.select())

//        while(queue.isNotEmpty()) {
//            val select = queue.removeFirst()
//            runSql("./sqlite3", select.toString(), workDir = ".")
//            val cov = getCoverage()
//            if (cov > coverage) {
//                println("Increased coverage: $cov > $coverage")
//                // todo mutate select & readd
//            }
//            coverage = max(coverage, cov)
//        }
    }
}