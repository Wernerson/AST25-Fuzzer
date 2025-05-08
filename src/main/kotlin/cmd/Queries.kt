package net.sebyte.tasks

import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator
import kotlin.random.Random

class QueriesTask : BasicQueryTask("queries", "Generate just queries.") {

    override fun execute() {
        val rand = seed?.let { Random(it) } ?: Random.Default
        val dataSources = createDataSources(rand, 10..20)
        val createSql = createDatabase(rand, dataSources)
        println(createSql)

        val selectGenerator = SelectGenerator(rand, dataSources)
        for (i in 0..numberOfQueries) {
            println(selectGenerator.select())
        }
    }
}