package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger

enum class Verdict {
    BUGGY, INTERESTING, BORING
}

interface Judicator {
    fun judge(query: Select, result: ExecResult): Verdict
}

object IgnorantJudicator : Judicator {
    override fun judge(query: Select, result: ExecResult) = Verdict.BORING
}

private val IGNORED_CODES = listOf(0, 1) // 0 = Success, 1 = Syntax Error

object ErrorCodeJudicator : Judicator {
    override fun judge(query: Select, result: ExecResult) = when (result) {
        is ExecResult.Error ->
            if (result.code in IGNORED_CODES) Verdict.BORING
            else Verdict.BUGGY

        ExecResult.Timeout,
        is ExecResult.Success -> Verdict.BORING
    }
}

class DifferentialJudicator(
    private val oracleExecutor: Executor
) : Judicator {
    override fun judge(query: Select, result: ExecResult): Verdict {
        if (result is ExecResult.Timeout) return Verdict.BORING
        val oracle = oracleExecutor.execute(query)
        return when(oracle) {
            ExecResult.Timeout -> Verdict.BORING
            is ExecResult.Error -> when {
                result is ExecResult.Success -> Verdict.INTERESTING
                result is ExecResult.Error && oracle.code != result.code -> Verdict.BUGGY
                else -> Verdict.BORING
            }
            is ExecResult.Success -> when {
                result is ExecResult.Error -> Verdict.BUGGY
                result is ExecResult.Success && oracle.output != result.output -> Verdict.BUGGY
                else -> Verdict.BORING
            }
        }
    }
}

class CoverageJudicator(
    private val judicator: Judicator,
    private val executable: String
) : Judicator {
    private var coverage = 0.0
    override fun judge(query: Select, result: ExecResult): Verdict {
        val verdict = judicator.judge(query, result)
        val cov = getCoverage(executable)

        return if (coverage < cov) {
            Logger.info { "Coverage increased: $coverage < $cov" }
            coverage = cov
            if (verdict == Verdict.BUGGY) Verdict.BUGGY
            else Verdict.INTERESTING
        } else verdict
    }
}