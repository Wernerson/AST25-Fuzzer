package net.sebyte.gen

import net.sebyte.ast.*
import kotlin.random.Random

class SelectGenerator(
    r: Random,
    private val sources: DataSources,
    private val depth: Int = 5
) : Generator(r) {

    fun with(
        depth: Int = this.depth
    ) = SelectGenerator(r, sources, depth)

    private var input: DataSources = emptyMap()

    fun tableOrSubquery(): Pair<TableOrSubquery, DataSources> = oneOf {
        val nonNullSources = sources.entries.filter { (key, _) -> key != null }
        if (nonNullSources.isNotEmpty()) {
            add {
                val table = oneOf(nonNullSources)
                TableOrSubquery.Table(tableName = table.key!!) to mapOf(table.key to table.value)
            }

            // todo table function call
        }
    }

    fun joinedClause(): Pair<JoinClause.JoinedClause, DataSources> {
        val operator = oneOf(JoinClause.JoinOperator.entries + null)
        val (tableOrSubquery, tables) = tableOrSubquery()
        val exprGenerator = ExprGenerator(r, tables)
        val constraint: JoinClause.JoinConstraint? = if (operator?.isNatural ?: true) null else oneOf {
            add { JoinClause.JoinConstraint.On(exprGenerator.expr()) }
            add {
                val columnNames = tables.flatMap { it.value }
                JoinClause.JoinConstraint.Using(columnNames)
            }
            add { null }
        }
        return JoinClause.JoinedClause(operator, tableOrSubquery, constraint) to tables
    }

    fun joinClause(): Pair<JoinClause, DataSources> {
        val (tableOrSubquery, src) = tableOrSubquery()
        val joinedClauses = listOf(1..3) { joinedClause() }
        val sources = joinedClauses.fold(src) { acc, (_, src) -> acc + src }
        return JoinClause(tableOrSubquery, joinedClauses.map { it.first }) to sources
    }

    fun from(): Pair<From, DataSources> = oneOf {
        add { joinClause() }
        add {
            val sources = listOf(1..3) { tableOrSubquery() }
            val source: DataSources = sources.fold(emptyMap()) { acc, (_, src) -> acc + src }
            TableOrSubqueries(sources.map { it.first }) to source
        }
    }

    fun orderingTerm(): OrderingTerm = OrderingTerm(
        expr = ExprGenerator(r, input).expr(),
        collateName = null, // todo
        direction = oneOf(OrderingTerm.Direction.entries + null),
        nulls = oneOf(OrderingTerm.Nulls.entries + null)
    )

    fun limit(): Limit = Limit(
        expr = ExprGenerator(r, input).expr(),
        offset = ExprGenerator(r, input).exprOrNull(0.5)
    )

    fun select(): Select {
        val (from, fromSources) = from()
        input = fromSources
        val exprGenerator = ExprGenerator(r, input)
        return Select(
            flag = oneOf(Select.Flag.DISTINCT, Select.Flag.ALL, null),
            resultColumns = ResultColumns.Star,
            from = from,
            where = exprGenerator.exprOrNull(0.2),
            groupBy = if (nextBoolean(0.2)) null else listOf(1..3) { exprGenerator.expr() },
            having = exprGenerator.exprOrNull(0.9),
            orderBy = if (nextBoolean(0.2)) null else listOf(1..3) { orderingTerm() },
            limit = if (nextBoolean(0.2)) null else limit()
        )
    }
}