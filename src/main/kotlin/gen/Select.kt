package net.sebyte.gen

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig
import kotlin.random.nextInt

typealias Tables = Map<String, List<Pair<String, DataType>>>

class SelectGenerator(
    cfg: GeneratorConfig,
    private val tables: Tables,
    private val depth: Int = cfg.maxSelectDepth
) : Generator(cfg) {

    private val constExprGenerator = ExprGenerator.constExprGenerator(cfg)

    fun orderingTerm(exprGenerator: ExprGenerator): OrderingTerm = OrderingTerm(
        expr = exprGenerator.expr(),
        collateName = null,
        direction = oneOf(OrderingTerm.Direction.entries + null),
        nulls = if (cfg.orderNulls) oneOf(OrderingTerm.Nulls.entries + null) else null
    )

    fun limit(): Limit {
        val exprGenerator = constExprGenerator.with(exprType = ExprType.INTEGER, depth = 3)
        return Limit(
            expr = exprGenerator.expr(),
            offset = exprGenerator.exprOrNull(cfg.offsetPct)
        )
    }

    fun resultColumns(input: DataSet): Pair<ResultColumns, DataSet> = oneOf {
        add {
            val columns = listOf(1..input.size) { oneOf(input) }.map { "ca${r.nextInt(1000..9999)}" to it }
            val exprs = columns.map { (alias, col) ->
                ResultColumns.ResultExpr(expr = TableColumn(table = col.scope, column = col.name), alias = alias)
            }
            ResultColumns.ExprList(exprs) to columns.map { (alias, col) -> DataEntry(".", alias, col.type) }
        }
    }

    fun select(outMap: OutputMap = mutableMapOf()): Select {
        val from = FromGenerator(cfg, tables, outMap, depth).from()
        val input = outMap[from]!!
        val exprGenerator = ExprGenerator(cfg, input)
        val (resultColumns, output) = resultColumns(input)
        val groupBy = if (nextBoolean(cfg.groupByPct)) {
            if (nextBoolean(0.8)) listOf(1..3) { TableColumn(column = oneOf(output).name) }
            else listOf(1..3) { exprGenerator.expr() }
        } else null
        val select = Select(
            flag = oneOf(Select.Flag.DISTINCT, Select.Flag.ALL, null),
            resultColumns = resultColumns,
            from = from,
            where = exprGenerator.exprOrNull(cfg.wherePct),
            groupBy = groupBy,
            having = if (groupBy != null) exprGenerator.exprOrNull(cfg.havingPct) else null,
            orderBy = if (nextBoolean(cfg.orderByPct)) listOf(1..3) { orderingTerm(exprGenerator) } else null,
            limit = if (nextBoolean(cfg.limitPct)) limit() else null
        )
        outMap[select] = output
        return select
    }
}