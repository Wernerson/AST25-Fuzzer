package net.sebyte

import kotlin.random.Random

class QueriesTask : BasicQueryTask("queries", "Generate just queries.") {

    override fun execute() {
        val rand = seed?.let { Random(it) } ?: Random.Default
        val dataSources = createDataSources(rand, 10..20)
        val createSql = createDatabase(rand, dataSources)
        println(createSql)

        for (i in 0..numberOfQueries) {
            println(Select.rand(rand, dataSources))
        }
    }
}