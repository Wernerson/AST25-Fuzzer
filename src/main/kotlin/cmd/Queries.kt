package net.sebyte.tasks

import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator
import kotlin.random.Random

class QueriesTask : BasicQueryTask("queries", "Generate and output queries.") {
    override fun execute() {
        val dataSources = createDataSources(cfg, 10..20)
        val createSql = createDatabase(cfg, dataSources)
        println(createSql)

        val selectGenerator = SelectGenerator(cfg, dataSources)
        for (i in 0..numberOfQueries) {
            println(selectGenerator.select())
        }
    }
}