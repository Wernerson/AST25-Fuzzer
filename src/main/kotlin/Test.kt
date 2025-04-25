package net.sebyte

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarBuilder
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import kotlin.random.Random

private const val NOT_TERMINATED = -69420 // random special code for non-termination
private val IGNORED_CODES = listOf(NOT_TERMINATED, 1, 0) // 1 = Syntax error, 0 = Success

fun runSql(
    sqlitePath: String,
    sql: String,
    workDir: String
): Triple<Int, String, String> {
    // create process
    val proc = ProcessBuilder(sqlitePath, "test.db")
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

fun store(
    workDir: String,
    bugDir: String,
    caseNumber: Int,
    query: String,
) {
    val dir = "$bugDir/bug_$caseNumber"
    File(dir).mkdirs()
    File("$workDir/test.db").copyTo(File("$dir/test.db"))
    File("$dir/original_test.sql").writeText(query)
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

    val pbb = ProgressBarBuilder()
        .setTaskName("Testing")
        .setUnit("tests", 1L)
        .showSpeed()
    for (i in ProgressBar.wrap(IntStream.range(1, config.numberOfQueries + 1), pbb)) {
        val query = Select.rand(rand, dataSources)
        val (testCode, testOut, testErr) = runSql(config.testPath, query, workDir = workDir)
        val (_, oracleOut, _) = runSql(config.oraclePath, query, workDir = workDir)

        if (testCode !in IGNORED_CODES) {
            println("Interesting return code in test case #$i! $testCode $testErr")
            store(workDir, config.bugDir, i, query)
        } else if (testCode == 0 && testOut != oracleOut) {
            println("Unequal output, comparing test case #$i...")
            val diff = compareResults(testOut, oracleOut)
            if (diff) {
                println("Found differences!")
                store(workDir, config.bugDir, i, query)
            } else println("False alarm.")
        }
    }
}

fun compareResults(a: String, b: String): Boolean {
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
        if (mapA[key] != mapB[key]) return true
    }
    return false
}