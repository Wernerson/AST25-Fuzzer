package net.sebyte

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val NOT_TERMINATED = -69420 // random special code for non-termination
private val IGNORED_CODES = listOf(NOT_TERMINATED, 1, 0) // 1 = Syntax error, 0 = Success

fun cmd(cmd: String): Triple<Int, String, String> {
    val proc = ProcessBuilder("/bin/sh", "-c", cmd)
        .directory(File("."))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
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
    database: String = "test.db",
    workDir: String = "."
): Triple<Int, String, String> {
    // create process
    val proc = ProcessBuilder(sqlitePath, database)
        .directory(File(workDir))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    // write sql as input
    proc.outputStream.bufferedWriter().use {
        it.write(sql)
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

fun test(config: Config) {
    val id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
    val rand = config.seed?.let { Random(it) } ?: Random.Default
    val dataSources = createDataSources(rand, 10..20)
    val createSql = createDatabase(rand, dataSources)

    val workDir = "./test_$id"
    File(workDir).mkdirs()
    File("$workDir/create.sql").writeText(createSql)
    runSql(config.testPath, createSql, workDir = workDir)

    for (i in 1..config.numberOfQueries) {
        val query = Select.rand(rand, dataSources)
        val (testCode, testOut, testErr) = runSql(config.testPath, query, workDir = workDir)
        val (_, oracleOut, _) = runSql(config.oraclePath, query, workDir = workDir)

        if (testCode !in IGNORED_CODES) {
            println("Interesting return code in query $i! $testCode $testErr")
            File("$workDir/original_test_$i.sql").writeText(query)
        } else if (testCode == 0 && testOut != oracleOut) {
            println("Unequal output, comparing $i...")
            compareResults(testOut, oracleOut)
            File("$workDir/original_test_$i.sql").writeText(query)
        }
    }
}

fun compareResults(a: String, b: String) {
    val mapA = a.lines().fold(mutableMapOf<String, Int>()) { map, line ->
        map[line] = map.getOrDefault(line, 0) + 1
        map
    }
    val mapB = b.lines().fold(mutableMapOf<String, Int>()) { map, line ->
        map[line] = map.getOrDefault(line, 0) + 1
        map
    }

    val keys = mapA.keys + mapB.keys
    for (key in keys) {
        if (mapA[key] != mapB[key]) println("Missmatch: $key ${mapA[key]} != ${mapB[key]}")
    }
}