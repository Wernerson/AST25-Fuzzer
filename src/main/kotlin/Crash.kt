package net.sebyte

private val IGNORED_CODES = listOf(NOT_TERMINATED, 0) // 0 = Success

class CrashTask : BasicTestTask("crash", "Test a single test subject on crashes.") {

    val codes = mutableMapOf<Int, Int>()

    override fun execute() {
        super.execute()

        println("Error codes:")
        for((code, count) in codes) {
            val pct = 100.0 * count / numberOfQueries
            val tag = when(code) {
                0 -> "success"
                1 -> "handled error"
                NOT_TERMINATED -> "not terminated"
                else -> "unknown"
            }
            println("$code ($tag): $count ($pct%)")
        }
    }

    override fun executeTest(query: String, caseNumber: Int) {
        val (code, _, err) = runSql(testPath, query, workDir = workDir)
        codes[code] = codes.getOrDefault(code, 0)+1

        if (code !in IGNORED_CODES && !err.startsWith("Error: near line")) {
            println("Interesting return code found!")
            println("Query:")
            println(query)
            println("Code: $code, Err: $err\n")
            println()
        }
    }
}