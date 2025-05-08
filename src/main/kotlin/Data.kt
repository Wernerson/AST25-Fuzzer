package net.sebyte

import net.sebyte.cfg.GeneratorConfig
import net.sebyte.gen.DataEntry
import net.sebyte.gen.Tables
import net.sebyte.gen.ExprGenerator
import kotlin.random.Random
import kotlin.random.nextInt

fun createDataSources(
    rand: Random, cfg: GeneratorConfig,
    noTables: IntRange = 5..10,
    noColumns: IntRange = 5..10,
): Tables = buildMap {
    for (i in 1..rand.nextInt(noTables)) {
        val columns = List(rand.nextInt(noColumns)) { "t${i}_c$it" }
        put("t$i", columns)
    }
}

fun createDatabase(rand: Random, cfg: GeneratorConfig, tables: Tables) = buildString {
    val constExprGenerator = ExprGenerator.constExprGenerator(cfg)
    tables.forEach { (name, columns) ->
        // create table
        append("CREATE TABLE $name (")
        append(columns.joinToString())
        append(");")
        appendLine()

        // create index
        for (i in 1..rand.nextInt(0..4)) {
            val exprGenerator = ExprGenerator(cfg, columns.map { DataEntry.Column(it) }, onlyDeterministic = true)
            append("CREATE ")
            if (rand.nextDouble() < 0.2) append("UNIQUE ")
            append("INDEX i${name}_$i ON $name (")
            append(List(rand.nextInt(1..5)) {
                exprGenerator.expr()
            }.joinToString())
            // TODO WHERE?
            append(");")
            appendLine()
        }

        // insert data
        append("INSERT INTO $name VALUES ")
        append(List(rand.nextInt(1..10)) {
            columns
                .map { constExprGenerator.expr() }
                .joinToString(prefix = "(", postfix = ")")
        }.joinToString())
        append(";")
        appendLine()
    }
}