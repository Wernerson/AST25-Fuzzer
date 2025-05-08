package net.sebyte.gen

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig

typealias Tables = Map<String, List<String>>

class SelectGenerator(
    cfg: GeneratorConfig,
    private val tables: Tables,
    private val depth: Int = cfg.maxSelectDepth
) : Generator(cfg) {

    private val constExprGenerator = ExprGenerator.constExprGenerator(cfg)
    private lateinit var exprGenerator: ExprGenerator
    lateinit var input: DataSet; private set
    lateinit var output: DataSet; private set

    fun orderingTerm(): OrderingTerm = OrderingTerm(
        expr = exprGenerator.expr(),
        collateName = null, // todo
        direction = oneOf(OrderingTerm.Direction.entries + null),
        nulls = if (cfg.orderNulls) oneOf(OrderingTerm.Nulls.entries + null) else null
    )

    fun limit(): Limit {
        val exprGenerator = constExprGenerator.with(allowedTypes = listOf(DataType.INTEGER))
        return Limit(
            expr = exprGenerator.expr(),
            offset = exprGenerator.exprOrNull(cfg.offsetPct)
        )
    }

    fun select(): Select {
        val (from, dataset) = FromGenerator(cfg, tables, depth).from()
        input = dataset
        exprGenerator = ExprGenerator(cfg, input)
        output = dataset
        return Select(
            flag = oneOf(Select.Flag.DISTINCT, Select.Flag.ALL, null),
            resultColumns = ResultColumns.Star,
            from = from,
            where = exprGenerator.exprOrNull(cfg.wherePct),
            groupBy = if (nextBoolean(cfg.groupByPct)) listOf(1..3) { exprGenerator.expr() } else null,
            having = exprGenerator.exprOrNull(cfg.havingPct),
            orderBy = if (nextBoolean(cfg.orderByPct)) listOf(1..3) { orderingTerm() } else null,
            limit = if (nextBoolean(cfg.limitPct)) limit() else null
        )
    }
}