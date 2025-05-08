package net.sebyte.tasks

import net.sebyte.cfg.SQLITE_v3_26_0
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator
import kotlin.random.Random

class QueriesTask : BasicQueryTask("queries", "Generate just queries.") {

    override fun execute() {
        val cfg = SQLITE_v3_26_0
        val rand = seed?.let { Random(it) } ?: Random.Default
        val dataSources = createDataSources(rand, cfg, 10..20)
        val createSql = createDatabase(rand, cfg, dataSources)
        println(createSql)

        val selectGenerator = SelectGenerator(cfg, dataSources)
        for (i in 0..numberOfQueries) {
            println(selectGenerator.select())
        }
    }
}