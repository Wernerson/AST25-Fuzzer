package net.sebyte.tasks

import net.sebyte.NOT_TERMINATED
import net.sebyte.cli.Logger
import net.sebyte.runSql

private val IGNORED_CODES = listOf(NOT_TERMINATED, 0, 1) // 0 = Success, 1 = Syntax Error

class CrashTask : BasicTestTask("crash", "Test a single test subject on crashes.") {

    private val codes = mutableMapOf<Int, Int>()

    override fun run() {
        super.run()

        Logger.info { "Error codes:" }
        for ((code, count) in codes) {
            val pct = 100.0 * count / numberOfQueries
            val tag = when (code) {
                0 -> "success"
                1 -> "handled error"
                NOT_TERMINATED -> "not terminated"
                else -> "unknown"
            }
            Logger.info { "$code ($tag): $count ($pct%)" }
        }
    }

    override fun executeTest(query: String, caseNumber: Int) {
        val (code, _, err) = runSql(testPath, query, workDir = workDir)
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
    }
}