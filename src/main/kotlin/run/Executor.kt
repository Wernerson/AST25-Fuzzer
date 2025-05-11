package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger
import runSql
import java.io.File

class DualError(code: Int, error: String, val other: ExecResult) : ExecResult.Error(code, error)
class ErrorWithCoverage(
    code: Int, error: String,
    val coverage: Double
) : ExecResult.Error(code, error)

fun ExecResult.Error.withCoverage(coverage: Double) = ErrorWithCoverage(code, error, coverage)

class DualSuccess(output: String, val other: ExecResult) : ExecResult.Success(output)
class SuccessWithCoverage(
    output: String,
    val coverage: Double
) : ExecResult.Success(output)

fun ExecResult.Success.withCoverage(coverage: Double) = SuccessWithCoverage(output, coverage)

interface Executor {
    fun execute(query: Select): ExecResult
}

private val NOOP = ExecResult.Success("")

object LogExecutor : Executor {
    override fun execute(query: Select): ExecResult {
        Logger.info { query }
        return NOOP
    }
}

class TestDbExecutor(
    protected val execPath: String,
    protected val testDb: File
) : Executor {
    override fun execute(query: Select): ExecResult {
        val result = runSql(execPath, query.toString(), testDb)
        when (result) {
            is ExecResult.Success -> {} // do nothing
            is ExecResult.Error -> Logger.info { "Error: ${result.error}" }
            ExecResult.Timeout -> Logger.info { "SQLite timed out." }
        }
        return result
    }
}

class OracleExecutor(
    protected val subjectPath: String,
    protected val oraclePath: String
) : Executor {
    override fun execute(query: Select): ExecResult {
        val sql = query.toString()
        val subject = runSql(subjectPath, sql)
        val oracle = runSql(oraclePath, sql)
        return when (subject) {
            ExecResult.Timeout -> ExecResult.Timeout
            is ExecResult.Error -> DualError(subject.code, subject.error, oracle)
            is ExecResult.Success -> DualSuccess(subject.output, oracle)
        }
    }
}

class CoverageExecutor(
    private val executor: Executor,
    private val execPath: String
) : Executor {
    override fun execute(query: Select) = executor.execute(query).let {
        when (it) {
            is ExecResult.Error -> it.withCoverage(getCoverage(execPath))
            is ExecResult.Success -> it.withCoverage(getCoverage(execPath))
            ExecResult.Timeout -> ExecResult.Timeout
        }
    }
}