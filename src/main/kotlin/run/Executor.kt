package net.sebyte.run

import ExecResult
import getCoverage
import net.sebyte.ast.Select
import net.sebyte.cli.Logger
import runSql
import java.io.File

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
            is ExecResult.Error -> Logger.debug { "Error: ${result.error}" }
            ExecResult.Timeout -> Logger.debug { "SQLite timed out." }
        }
        return result
    }
}
