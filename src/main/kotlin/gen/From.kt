package net.sebyte.gen

import net.sebyte.ast.From
import net.sebyte.ast.JoinClause
import net.sebyte.ast.TableOrSubqueries
import net.sebyte.ast.TableOrSubquery
import net.sebyte.cfg.GeneratorConfig
import kotlin.random.nextInt

class FromGenerator(
    cfg: GeneratorConfig,
    private val tables: Tables,
    val outMap: OutputMap,
    private val depth: Int = cfg.maxFromDepth
) : Generator(cfg) {

    fun tableOrSubquery(): TableOrSubquery = oneOf {
        add {
            val table = oneOf(tables.entries)
            val alias = "ta${r.nextInt(1000..9999)}"
            val tableOrSubquery = TableOrSubquery.Table(tableName = table.key, alias = alias)
            outMap[tableOrSubquery] = table.value.map { (name, type) -> DataEntry(alias, name, type) }
            tableOrSubquery
        }

        if (depth > 0) {
            add {
                val selectGen = SelectGenerator(cfg, tables, depth - 1)
                val select = selectGen.select(outMap)
                val output = outMap[select]!!
                val alias = "sa${r.nextInt(1000..9999)}"
                val subquery = TableOrSubquery.Subquery(select, alias)
                outMap[subquery] = output.map { DataEntry(alias, it.name, it.type) }
                subquery
            }
        }
    }

    fun joinedClause(tableOrSubquery: TableOrSubquery, dataset: DataSet): JoinClause.JoinedClause {
        val operator = oneOf(cfg.supportedJoinOperators)
        val constraint: JoinClause.JoinConstraint? = if (operator.isNatural) null else oneOf {
            val exprGenerator = ExprGenerator(cfg, dataset, exprType = ExprType.INTEGER)
            add { JoinClause.JoinConstraint.On(exprGenerator.expr()) }
            add { null }
        }
        return JoinClause.JoinedClause(operator, tableOrSubquery, constraint)
    }

    fun joinClause(): JoinClause {
        val table = tableOrSubquery()
        var dataset = outMap[table]!!
        val joinedClauses = listOf(1..3) {
            val nextTable = tableOrSubquery()
            val nextDataset = outMap[nextTable]!!
            dataset += nextDataset
            joinedClause(nextTable, dataset)
        }
        val join = JoinClause(table, joinedClauses)
        outMap[join] = dataset
        return join
    }

    fun from(): From = oneOf {
        add { joinClause() }
        add {
            val sources = listOf(1..3) { tableOrSubquery() }
            val tableOrSubqueries = TableOrSubqueries(sources)
            outMap[tableOrSubqueries] = sources.fold(emptyList()) { acc, src -> acc + outMap[src]!! }
            tableOrSubqueries
        }
    }
}