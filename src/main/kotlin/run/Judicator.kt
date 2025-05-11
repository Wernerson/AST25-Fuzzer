package net.sebyte.run

import ExecResult
import net.sebyte.cli.Logger

enum class Verdict {
    BUGGY, INTERESTING, UNINTERESTING
}

interface Judicator {
    fun judge(result: ExecResult): Verdict
}

object IgnorantJudicator : Judicator {
    override fun judge(result: ExecResult) = Verdict.UNINTERESTING
}

private val IGNORED_CODES = listOf(0, 1) // 0 = Success, 1 = Syntax Error

object ErrorCodeJudicator : Judicator {
    override fun judge(result: ExecResult) = when (result) {
        is ExecResult.Error ->
            if (result.code in IGNORED_CODES) Verdict.UNINTERESTING
            else Verdict.BUGGY

        ExecResult.Timeout,
        is ExecResult.Success -> Verdict.UNINTERESTING
    }
}

object ComparisonJudicator : Judicator {
    override fun judge(result: ExecResult): Verdict = when (result) {
        ExecResult.Timeout -> Verdict.UNINTERESTING

        is DualError ->
            if (result.code in IGNORED_CODES) Verdict.UNINTERESTING
            else if (result.other !is ExecResult.Error) Verdict.BUGGY
            else if (result.code != result.other.code) Verdict.BUGGY
            else Verdict.UNINTERESTING

        is DualSuccess ->
            if (result.other !is ExecResult.Success) Verdict.BUGGY
            else if (result.output != result.other.output) Verdict.BUGGY
            else Verdict.UNINTERESTING

        is ExecResult.Success -> Verdict.UNINTERESTING
        is ExecResult.Error ->
            if (result.code in IGNORED_CODES) Verdict.UNINTERESTING
            else Verdict.INTERESTING
    }
}

class CoverageJudicator(
    private val judicator: Judicator
) : Judicator {
    private var coverage = 0.0
    override fun judge(result: ExecResult): Verdict {
        val verdict = judicator.judge(result)

        val cov = when (result) {
            is ErrorWithCoverage -> result.coverage
            is SuccessWithCoverage -> result.coverage
            else -> coverage
        }

        return if (coverage < cov) {
            Logger.info { "Coverage increased: $coverage < $cov" }
            coverage = cov
            if (verdict == Verdict.BUGGY) Verdict.BUGGY
            else Verdict.INTERESTING
        } else verdict
    }
}