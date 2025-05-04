package net.sebyte.gen

import net.sebyte.ast.From
import net.sebyte.ast.JoinClause
import net.sebyte.ast.TableOrSubqueries
import net.sebyte.ast.TableOrSubquery
import kotlin.random.Random

class FromGenerator(
    r: Random,
    private val tables: Tables
) : Generator(r) {

    fun tableOrSubquery(): Pair<TableOrSubquery, DataSet> = oneOf {
        add {
            val table = oneOf(tables.entries)
            TableOrSubquery.Table(tableName = table.key) to table.value.map { DataEntry.ScopedColumn(table.key, it) }
        }
        // todo table function call
    }

    fun joinedClause(tableOrSubquery: TableOrSubquery, dataset: DataSet): JoinClause.JoinedClause {
        val operator = oneOf(JoinClause.JoinOperator.entries + null)
        val constraint: JoinClause.JoinConstraint? = if (operator?.isNatural ?: true) null else oneOf {
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