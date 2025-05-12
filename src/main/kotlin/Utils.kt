import java.io.File
import java.util.concurrent.TimeUnit

sealed interface ExecResult {
    object Timeout : ExecResult
    open class Success(val output: String) : ExecResult
    open class Error(val code: Int, val error: String) : ExecResult
}

fun runCmd(
    command: String,
    input: String? = null,
    workDir: File = File("."),
    timeout: Long = 5,
) = runCmd(listOf(command), input, workDir)

fun runCmd(
    commands: List<String>,
    input: String? = null,
    workDir: File = File("."),
    timeout: Long = 5,
): ExecResult {
    // create process
    val proc = ProcessBuilder(commands)
        .directory(workDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    // write sql as input
    proc.outputStream.bufferedWriter().use {
        if (input != null) it.write(input)
    }
    // wait for result
    val finished = proc.waitFor(timeout, TimeUnit.SECONDS)
    return if (!finished) {
        proc.destroy()
        ExecResult.Timeout
    } else {
        val error = proc.errorStream.bufferedReader().readText()
        val output = proc.inputStream.bufferedReader().readText()
        val exit = proc.exitValue()
        if (exit == 0) ExecResult.Success(output)
        else ExecResult.Error(exit, error)
    }
}

fun runSql(
    executable: String,
    sql: String,
    testDb: File? = null,
    timeout: Long = 5,
): ExecResult =
    if (testDb != null) runCmd(listOf(executable, testDb.absolutePath), sql, timeout = timeout)
    else runCmd(executable, sql, timeout = timeout)

private val coverageRegex = "^Lines executed:(\\d+\\.?\\d*)% of \\d+$".toRegex()
fun getCoverage(executable: String): Double {
    val result = runCmd(listOf("gcov", "-r", executable))

    val out = when (result) {
        is ExecResult.Timeout -> error("gcov never terminated!")
        is ExecResult.Error -> error("gcov returned an error!\nError (${result.code}): ${result.error}")
        is ExecResult.Success -> result.output
    }

    val line = out.trim().lines().last()
    val match = coverageRegex.find(line)
    val coverage = match?.groups[1]?.value?.toDoubleOrNull()
    checkNotNull(coverage) {
        """
            gcov output could not be parsed correctly!
            Output:
            $out
        """.trimIndent()
    }
    return coverage
}
