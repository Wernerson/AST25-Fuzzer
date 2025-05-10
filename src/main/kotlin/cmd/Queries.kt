package net.sebyte.tasks

import net.sebyte.cli.Logger
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import net.sebyte.gen.SelectGenerator

class QueriesTask : BasicQueryTask("queries", "Generate and output queries.") {
    override fun run() {
        val dataSources = createDataSources(cfg, 10..20)
        val createSql = createDatabase(cfg, dataSources)
        Logger.info { createSql }

        val selectGenerator = SelectGenerator(cfg, dataSources)
        for (i in 0..numberOfQueries) {
            Logger.info { selectGenerator.select() }
        }
    }
}