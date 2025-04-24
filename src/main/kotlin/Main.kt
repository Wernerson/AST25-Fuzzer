package net.sebyte

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val NOT_TERMINATED = -69420 // random special code for non-termination
private const val NUM_QUERIES = 100_000
private const val TEST_VERSION = "3.26.0"
private const val ORACLE_VERSION = "3.39.4"

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

fun main() {
    val id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
    val rand = Random.Default
    val dataSources = createDataSources(rand, 10..20)
    val preamble = createDatabase(rand, dataSources)

    val preambleFile = "./preamble_$id.sql"
    File(preambleFile).writeText(preamble)
    cmd("/usr/bin/sqlite3-$TEST_VERSION ./test_$id.db < $preambleFile")
    cmd("/usr/bin/sqlite3-$ORACLE_VERSION ./orcl_$id.db < $preambleFile") // TODO only use 1 db

    for (i in 0..NUM_QUERIES) {
        val (query, _) = Select.rand(rand, dataSources)
        val sqlFile = "./query_${id}_$i.sql"
        File(sqlFile).writeText(query.toString())
        val (testCode, testOut, testErr) = cmd("/usr/bin/sqlite3-$TEST_VERSION ./test_$id.db < $sqlFile")
        val (_, oracleOut, _) = cmd("/usr/bin/sqlite3-$ORACLE_VERSION ./orcl_$id.db < $sqlFile")
        if (testCode !in IGNORED_CODES) println("Interesting return code! $testCode $testErr")
        if (testCode == 0 && testOut != oracleOut ) {
            println("Comparing $sqlFile")
            compareResults(testOut, oracleOut)
        }
    }
}

fun compareResults(a: String, b: String) {
    val mapA = a.lines().fold(mutableMapOf<String, Int>()) { map, line ->
        map[line] = map.getOrDefault(line, 0)+1
        map
    }
    val mapB = b.lines().fold(mutableMapOf<String, Int>()) { map, line ->
        map[line] = map.getOrDefault(line, 0)+1
        map
    }

    val keys = mapA.keys + mapB.keys
    for(key in keys) {
        if (mapA[key] != mapB[key]) println("Missmatch: $key ${mapA[key]} != ${mapB[key]}")
    }
}