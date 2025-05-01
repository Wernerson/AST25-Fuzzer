package net.sebyte

private val IGNORED_CODES = listOf(NOT_TERMINATED, 0) // 0 = Success

class CrashTask : BasicTestTask("crash", "Test a single test subject on crashes.") {

    override fun executeTest(query: String, caseNumber: Int) {
        val (testCode, _, testErr) = runSql(testPath, query, workDir = workDir)
        if (testCode !in IGNORED_CODES && !testErr.startsWith("Error: near line")) {
            println("Interesting return code found!")
            println("Code: $testCode, Err: $testErr\n")
        }
    }
}