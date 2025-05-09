package net.sebyte.gen

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig

typealias Tables = Map<String, List<Pair<String, DataType>>>

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
        val exprGenerator = constExprGenerator.with(exprType = ExprType.INTEGER)
        return Limit(
            expr = exprGenerator.expr(),
            offset = exprGenerator.exprOrNull(cfg.offsetPct)
        )
    }

    fun resultColumns(): Pair<ResultColumns, DataSet> = oneOf {
        add { ResultColumns.Star to input }
//        add {
//
//            ResultColumns.ExprList(exprs = listOf(ResultColumns.Expr()))
//        }
        if (input.any { it is DataEntry.ScopedColumn }) add {
            val scopes = input.filterIsInstance<DataEntry.ScopedColumn>()
            val table = oneOf(scopes)
            ResultColumns.TableStar(table = table.scope) to scopes.filter { table.scope == it.scope }
        }
    }

    fun select(): Select {
        val (from, inputSet) = FromGenerator(cfg, tables, depth).from()
        input = inputSet
        exprGenerator = ExprGenerator(cfg, input)
        val (resultColumns, outputSet) = resultColumns()
        output = outputSet
        val groupBy = if (nextBoolean(cfg.groupByPct)) {
            if (nextBoolean(0.8)) listOf(1..3) { TableColumn(column = oneOf(input).name) }
            else listOf(1..3) { exprGenerator.expr() }
        } else null
        return Select(
            flag = oneOf(Select.Flag.DISTINCT, Select.Flag.ALL, null),
            resultColumns = resultColumns,
            from = from,
            where = exprGenerator.exprOrNull(cfg.wherePct),
            groupBy = groupBy,
            having = if (groupBy != null) exprGenerator.exprOrNull(cfg.havingPct) else null,
            orderBy = if (nextBoolean(cfg.orderByPct)) listOf(1..3) { orderingTerm() } else null,
            limit = if (nextBoolean(cfg.limitPct)) limit() else null
        )
    }
}