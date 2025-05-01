package net.sebyte

import java.io.File
import java.util.concurrent.TimeUnit

const val NOT_TERMINATED = -69420 // random special code for non-termination

fun runCmd(
    cmd: String,
    args: String,
    input: String? = null,
    workDir: String = "."
): Triple<Int, String, String> {
    // create process
    val proc = ProcessBuilder(cmd, args)
        .directory(File(workDir))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    // write sql as input
    proc.outputStream.bufferedWriter().use {
        if (input != null) it.write(input)
    }
    // wait for result
    val finished = proc.waitFor(5, TimeUnit.SECONDS)
    return if (!finished) {
        proc.destroy()
        Triple(NOT_TERMINATED, "", "")
    } else {
        val error = proc.errorStream.bufferedReader().readText()
        val output = proc.inputStream.bufferedReader().readText()
        val exit = proc.exitValue()
        Triple(exit, output, error)
    }
}

fun runSql(
    sqlitePath: String,
    sql: String,
    workDir: String
): Triple<Int, String, String> = runCmd(sqlitePath, "test.db", sql, workDir)