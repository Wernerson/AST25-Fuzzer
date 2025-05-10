package net.sebyte.tasks

import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.optional
import net.sebyte.NOT_TERMINATED
import net.sebyte.cli.Logger
import net.sebyte.runCmd
import net.sebyte.runSql
import java.io.File


private val IGNORED_CODES = listOf(NOT_TERMINATED, 1, 0) // 1 = Syntax error, 0 = Success

class CompareTask : BasicTestTask("compare", "Compare test subject output with test oracle.") {

    val oraclePath by argument(
        ArgType.String, "oraclePath", "Path to test oracle"
    ).optional().default("/usr/bin/sqlite3-3.39.4")

    private fun compareResults(a: String, b: String): Boolean {
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

    private fun store(
        workDir: String,
        caseNumber: Int,
        query: String
    ) {
        val dir = "$bugDir/bug_$caseNumber"
        val (_, testVersion, _) = runCmd(listOf(testPath, "--version"))
        val (_, oracleVersion, _) = runCmd(listOf(oraclePath, "--version"))
        val checkFile = javaClass.getResource("/check.sh")!!
            .readText()
            .replace("\${TEST_PATH}", testPath)
            .replace("\${ORACLE_PATH}", oraclePath)

        val readme = javaClass.getResource("/README.md")!!
            .readText()
            .replace("\${TEST_VERSION}", testVersion)
            .replace("\${ORACLE_VERSION}", oracleVersion)

        File(dir).mkdirs()
        File("$workDir/test.db").copyTo(File("$dir/test.db"))
        File("$dir/original_test.sql").writeText(query)
        File("$dir/reduced_test.sql").writeText(query)
        File("$dir/version.txt").writeText(testVersion)
        File("$dir/README.md").writeText(readme)
        File("$dir/check.sh").apply {
            writeText(checkFile)
            setExecutable(true)
        }
    }

    override fun executeTest(query: String, caseNumber: Int) {
        val (testCode, testOut, testErr) = runSql(testPath, query, workDir = workDir)
        val (_, oracleOut, _) = runSql(oraclePath, query, workDir = workDir)

        if (testCode !in IGNORED_CODES) {
            Logger.info { "Interesting return code in test case #$caseNumber! $testCode $testErr" }
            store(workDir, caseNumber, query)
        } else if (testCode == 0 && testOut != oracleOut) {
            Logger.debug { "Unequal output, comparing test case #$caseNumber..." }
            val diff = compareResults(testOut, oracleOut)
            if (diff) {
                Logger.info { "Found differences!" }
                store(workDir, caseNumber, query)
            } else Logger.debug { "False alarm." }
        }
    }

}