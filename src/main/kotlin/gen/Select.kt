package net.sebyte.gen

import net.sebyte.ast.*
import kotlin.random.Random

typealias Tables = Map<String, List<String>>

class SelectGenerator(
    r: Random,
    private val tables: Tables,
    private val depth: Int = 3
) : Generator(r) {

    private val constExprGenerator = ExprGenerator.constExprGenerator(r)
    private lateinit var exprGenerator: ExprGenerator
    lateinit var input: DataSet; private set
    lateinit var output: DataSet; private set

    fun orderingTerm(): OrderingTerm = OrderingTerm(
        expr = exprGenerator.expr(),
        collateName = null, // todo
        direction = oneOf(OrderingTerm.Direction.entries + null),
        nulls = oneOf(OrderingTerm.Nulls.entries + null)
    )

    fun limit(): Limit {
        val exprGenerator = constExprGenerator.with(allowedTypes = listOf(DataType.INTEGER))
        return Limit(
            expr = exprGenerator.expr(),
            offset = exprGenerator.exprOrNull(0.5)
        )
    }

    fun select(): Select {
        val (from, dataset) = FromGenerator(r, tables, depth).from()
        input = dataset
        exprGenerator = ExprGenerator(r, input)
        output = dataset
        return Select(
            flag = oneOf(Select.Flag.DISTINCT, Select.Flag.ALL, null),
            resultColumns = ResultColumns.Star,
            from = from,
            where = exprGenerator.exprOrNull(0.2),
            groupBy = if (nextBoolean(0.2)) null else listOf(1..3) { exprGenerator.expr() },
//            having = exprGenerator.exprOrNull(0.9),
            orderBy = if (nextBoolean(0.2)) null else listOf(1..3) { orderingTerm() },
            limit = if (nextBoolean(0.2)) null else limit()
        )
    }
}