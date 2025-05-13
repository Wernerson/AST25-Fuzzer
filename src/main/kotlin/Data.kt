package net.sebyte

import net.sebyte.ast.DataType
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.gen.DataEntry
import net.sebyte.gen.ExprGenerator
import net.sebyte.gen.ExprType
import net.sebyte.gen.Tables
import kotlin.random.nextInt

fun createDataSources(
    cfg: GeneratorConfig,
    noTables: Int,
    noColumns: Int,
): Tables = buildMap {
    for (i in 1..noTables) {
        val columns = List(noColumns) { "t${i}_c$it" to DataType.entries.random(cfg.r) }
        put("t$i", columns)
    }
}

fun createDatabase(cfg: GeneratorConfig, tables: Tables) = buildString {
    val constExprGenerator = ExprGenerator.constExprGenerator(cfg)
    tables.forEach { (name, columns) ->
        // create table
        append("CREATE TABLE $name (")
        append(columns.joinToString { (name, _) -> name })
        append(");")
        appendLine()

        // create index
        for (i in 1..cfg.r.nextInt(0..4)) {
            val exprGenerator = ExprGenerator(
                cfg,
                columns.map { (col, type) -> DataEntry(null, col, type) },
                exprType = ExprType(listOf(DataType.INTEGER, DataType.REAL, DataType.BLOB), true),
                onlyDeterministic = true,
            )
            append("CREATE ")
            if (cfg.r.nextDouble() < 0.2) append("UNIQUE ")
            append("INDEX i${name}_$i ON $name (")
            append(List(cfg.r.nextInt(1..5)) {
                exprGenerator.expr()
            }.joinToString())
            if (cfg.r.nextBoolean()) append(" WHERE ${exprGenerator.expr()}")
            append(");")
            appendLine()
        }

        // insert data
        append("INSERT INTO $name VALUES ")
        append(List(cfg.r.nextInt(1..10)) {
            columns
                .map { constExprGenerator.expr() }
                .joinToString(prefix = "(", postfix = ")")
        }.joinToString())
        append(";")
        appendLine()
    }
}