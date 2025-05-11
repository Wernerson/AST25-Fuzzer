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
    val ioMap: IOMap,
    private val depth: Int = cfg.maxFromDepth
) : Generator(cfg) {

    fun tableOrSubquery(): Pair<TableOrSubquery, DataSet> = oneOf {
        add {
            val table = oneOf(tables.entries)
            val alias = "ta${r.nextInt(1000..9999)}"
            val dataset = table.value.map { (name, type) -> DataEntry(alias, name, type) }
            TableOrSubquery.Table(tableName = table.key, alias = alias) to dataset
        }

        if (depth > 0) {
            add {
                val selectGen = SelectGenerator(cfg, tables, depth - 1)
                val subquery = selectGen.select(ioMap)
                val output = ioMap[subquery]!!.second
                val alias = "sa${r.nextInt(1000..9999)}"
                val dataset = output.map { DataEntry(alias, it.name, it.type) }
                TableOrSubquery.Subquery(subquery, alias) to dataset
            }

//            add {
//                val (from, ds) = with(depth - 1).from()
//                TableOrSubquery.NestedFrom(from) to ds
//            }
        }
        // todo table function call
    }

    fun joinedClause(tableOrSubquery: TableOrSubquery, dataset: DataSet): JoinClause.JoinedClause {
        val operator = oneOf(cfg.supportedJoinOperators)
        val constraint: JoinClause.JoinConstraint? = if (operator.isNatural) null else oneOf {
            val exprGenerator = ExprGenerator(cfg, dataset, exprType = ExprType.INTEGER)
            add { JoinClause.JoinConstraint.On(exprGenerator.expr()) }
//            add {
//                val columnNames = tables.flatMap { it.value }
//                JoinClause.JoinConstraint.Using(listOf(1..5, columnNames))
//            }
            add { null }
        }
        return JoinClause.JoinedClause(operator, tableOrSubquery, constraint)
    }

    fun joinClause(): Pair<JoinClause, DataSet> {
        var (table, dataset) = tableOrSubquery()
        val joinedClauses = listOf(1..3) {
            val (nextTable, nextDataset) = tableOrSubquery()
            dataset += nextDataset
            joinedClause(nextTable, dataset)
        }
        return JoinClause(table, joinedClauses) to dataset
    }

    fun from(): Pair<From, DataSet> = oneOf {
        add { joinClause() }
        add {
            val sources = listOf(1..3) { tableOrSubquery() }
            val dataset: DataSet = sources.fold(emptyList()) { acc, (_, src) -> acc + src }
            TableOrSubqueries(sources.map { it.first }) to dataset
        }
    }
}