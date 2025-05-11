package net.sebyte.run

import net.sebyte.cli.pbar

class Trial(
    private val legislator: Legislator,
    private val executor: Executor,
    private val judicator: Judicator,
    private val clerk: Clerk
) {
    fun run() {
        for (query in legislator.pbar()) {
            val result = executor.execute(query)
            val verdict = judicator.judge(result)
            clerk.report(query, result, verdict)
            legislator.notice(query, verdict)
        }
        clerk.summarise()
    }
}