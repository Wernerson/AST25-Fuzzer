package net.sebyte.gen

import net.sebyte.ast.*
import kotlin.random.Random

typealias Tables = Map<String, List<String>>

class SelectGenerator(
    r: Random,
    private val tables: Tables,
    private val depth: Int = 5
) : Generator(r) {

    private val constExprGenerator = ExprGenerator.constExprGenerator(r)

    fun with(
        depth: Int = this.depth
    ) = SelectGenerator(r, tables, depth)

    private var input: DataSet = emptyList()

    fun orderingTerm(): OrderingTerm = OrderingTerm(
        expr = ExprGenerator(r, input).expr(),
        collateName = null, // todo
        direction = oneOf(OrderingTerm.Direction.entries + null),
        nulls = oneOf(OrderingTerm.Nulls.entries + null)
    )

    fun limit(): Limit = Limit(
        expr = constExprGenerator.expr(),
        offset = constExprGenerator.exprOrNull(0.5)
    )

    fun select(): Select {
        val (from, dataset) = FromGenerator(r, tables).from()
        input = dataset
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