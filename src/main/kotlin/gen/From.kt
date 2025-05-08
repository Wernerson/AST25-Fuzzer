package net.sebyte.gen

import net.sebyte.ast.From
import net.sebyte.ast.JoinClause
import net.sebyte.ast.TableOrSubqueries
import net.sebyte.ast.TableOrSubquery
import kotlin.random.Random
import kotlin.random.nextInt

class FromGenerator(
    r: Random,
    private val tables: Tables,
    private val depth: Int = 3
) : Generator(r) {

    fun tableOrSubquery(): Pair<TableOrSubquery, DataSet> = oneOf {
        add {
            val table = oneOf(tables.entries)
            TableOrSubquery.Table(tableName = table.key) to table.value.map { DataEntry.ScopedColumn(table.key, it) }
        }

        if (depth > 0) {
            add {
                val selectGen = SelectGenerator(r, tables, depth - 1)
                val subquery = selectGen.select()
                val alias = "a${r.nextInt(100..999)}"
                val dataset = selectGen.output.map { DataEntry.ScopedColumn(alias, it.name) }
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
        val operator = oneOf(JoinClause.JoinOperator.entries)
        val constraint: JoinClause.JoinConstraint? = if (operator.isNatural) null else oneOf {
            val exprGenerator = ExprGenerator(r, dataset)
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