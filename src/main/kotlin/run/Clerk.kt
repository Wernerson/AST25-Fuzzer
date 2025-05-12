package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger
import java.io.File

interface Clerk {
    fun report(query: Select, result: ExecResult, verdict: Verdict) {}
    fun summarise() {}
}

open class BaseClerk(
    private val archiveDir: File? = null,
    private val testDb: File? = null
) : Clerk {
    protected val codes = mutableMapOf<Int, Int>()
    protected var total = 0
    protected var successes = 0
    protected var errors = 0
    protected var nonTerminations = 0
    protected var bugs = 0

    override fun report(
        query: Select,
        result: ExecResult,
        verdict: Verdict
    ) {
        ++total
        when (result) {
            is ExecResult.Error -> {
                ++errors
                codes[result.code] = codes.getOrDefault(result.code, 0) + 1
            }

            is ExecResult.Success -> ++successes
            ExecResult.Timeout -> ++nonTerminations
        }

        if (verdict == Verdict.BUGGY) archive(query, result)
    }

    open fun archive(query: Select, result: ExecResult) {
        ++bugs
        Logger.debug { "Buggy Query:\n$query" }
        if (archiveDir != null) {
            val dir = File("$archiveDir/bug_$bugs/")
            dir.mkdirs()
            File("$dir/original_query.sql").writeText(query.toString())
            testDb?.copyTo(File("$dir/test.db"), overwrite = true)
            File("$dir/output.txt").writeText(when(result) {
                is ExecResult.Error -> result.error + "\n" + result.error
                is ExecResult.Success -> result.output
                ExecResult.Timeout -> "Timeout."
            })
        }
    }

    override fun summarise() {
        Logger.info {
            buildString {
                appendLine(
                    """
                    Total: $total
                    Successes: $successes (${100.0 * successes / total}%)
                    Errors: $errors (${100.0 * errors / total}%)
                    Bugs: $bugs (${100.0 * bugs / total}%)
                    Not terminated: $nonTerminations (${100.0 * nonTerminations / total}%)
                    """.trimIndent()
                )
                appendLine("Error codes:")
                for ((code, count) in codes) appendLine("  $code: $count (${100.0 * count / errors}% or errors)")
            }
        }
    }
}

class CoverageClerk(
    private val execPath: String,
    archiveDir: File? = null
) : BaseClerk(archiveDir) {
    private val baseline = getCoverage(execPath)

    override fun summarise() {
        super.summarise()
        Logger.info {
            """
            Baseline: $baseline
            Coverage: ${getCoverage(execPath)}
            """.trimIndent()
        }
    }
}