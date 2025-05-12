package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger
import java.io.File
import kotlin.math.max

interface Clerk {
    fun report(query: Select, result: ExecResult, verdict: Verdict) {}
    fun summarise() {}
}

object IgnorantClerk : Clerk

open class SummaryClerk(
    protected val outputFile: File? = null
) : Clerk {
    protected val codes = mutableMapOf<Int, Int>()
    protected var total = 0
    protected var successes = 0
    protected var errors = 0
    protected var nonTerminations = 0

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
    }

    override fun summarise() {
        if (outputFile == null) Logger.info {
            buildString {
                appendLine(
                    """
                    Total: $total
                    Successes: $successes
                    Errors: $errors
                    Not terminated: $nonTerminations
                    """.trimIndent()
                )
                appendLine("Error codes:")
                for ((code, count) in codes) appendLine("  $code: $count (${count / errors})")
            }
        }
    }
}

class CoverageClerk(
    outputFile: File? = null,
    private val execPath: String
) : SummaryClerk(outputFile) {
    private val baseline = getCoverage(execPath)

    override fun summarise() {
        if (outputFile == null) Logger.info {
            buildString {
                appendLine(
                    """
                    Total: $total
                    Successes: $successes
                    Errors: $errors
                    Not terminated: $nonTerminations
                    """.trimIndent()
                )
                appendLine("Baseline: $baseline")
                appendLine("Coverage: ${getCoverage(execPath)}")
                appendLine("Error codes:")
                for ((code, count) in codes) appendLine("  $code: $count (${count / errors})")
            }
        }
    }
}