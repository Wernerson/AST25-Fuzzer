package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger

interface Clerk {
    fun report(query: Select, result: ExecResult, verdict: Verdict) {}
    fun summarise() {}
}

open class BaseClerk : Clerk {
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
        Logger.info {
            buildString {
                appendLine(
                    """
                    Total: $total
                    Successes: $successes (${successes / total}%)
                    Errors: $errors (${errors / total}%)
                    Not terminated: $nonTerminations (${nonTerminations / total}%)
                    """.trimIndent()
                )
                appendLine("Error codes:")
                for ((code, count) in codes) appendLine("  $code: $count (${count / errors}% or errors)")
            }
        }
    }
}

class CoverageClerk(
    private val execPath: String,
) : BaseClerk() {
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