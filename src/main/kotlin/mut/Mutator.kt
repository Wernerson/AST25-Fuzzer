package net.sebyte.mut

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.gen.ExprGenerator
import net.sebyte.gen.ExprType
import net.sebyte.gen.FromGenerator
import net.sebyte.gen.Generator
import net.sebyte.gen.IOMap
import net.sebyte.gen.Tables
import kotlin.random.Random.Default.nextBoolean

class Mutator(
    cfg: GeneratorConfig,
    private val tables: Tables
): Generator(cfg) {

    private val mutPct = 0.1
    private fun <T> keep(block: () -> T) = block
    private infix fun <T> (() -> T).mutate(block: () -> T) = if (r.nextDouble() < mutPct) block() else this()

    fun ExprGenerator.mutate(e: Expr): Expr = when (e) {
        is LiteralValue -> keep { e } mutate { expr() }
        is TableColumn -> keep { e } mutate { expr() }
        is UnaryExpr -> keep { UnaryExpr(e.op, mutate(e.expr)) } mutate { expr() }
        is BinaryExpr -> keep { BinaryExpr(mutate(e.left), e.op, mutate(e.right)) } mutate { expr() }
        is FunctionCall -> keep {
            FunctionCall(e.name, e.args.map { mutate(it) }, e.filterWhere?.let { mutate(it) } ?: exprOrNull())
        } mutate { expr() }
    }

    fun FromGenerator.mutate(t: TableOrSubquery): TableOrSubquery = when (t) {
        is TableOrSubquery.Table -> keep { t } mutate { tableOrSubquery().first }
        is TableOrSubquery.Subquery -> keep { TableOrSubquery.Subquery(mutate(t.select, ioMap)) } mutate { tableOrSubquery().first }
    }

    fun FromGenerator.mutate(j: JoinClause.JoinedClause) = JoinClause.JoinedClause(
        operator = keep { j.operator } mutate { oneOf(cfg.supportedJoinOperators) },
        tableOrSubquery = keep { mutate(j.tableOrSubquery) } mutate { tableOrSubquery().first },
        constraint = keep { j.constraint } mutate {
            val exprGen = ExprGenerator.constExprGenerator(cfg).with(exprType = ExprType.INTEGER)
            when (j.constraint) {
                is JoinClause.JoinConstraint.On -> JoinClause.JoinConstraint.On(exprGen.mutate(j.constraint.expr))
                null -> exprGen.exprOrNull()?.let { JoinClause.JoinConstraint.On(it) }
                is JoinClause.JoinConstraint.Using -> TODO()
            }
        }
    )

    fun FromGenerator.mutate(f: From): From = when (f) {
        is JoinClause -> keep {
            JoinClause(mutate(f.tableOrSubquery), f.joinedClauses.map { mutate(it) })
        } mutate { from().first }

        is TableOrSubqueries -> keep { TableOrSubqueries(f.tableOrSubqueries.map { mutate(it) }) } mutate { from().first }
    }

    fun mutate(s: Select, ioMap: IOMap): Select {
        val fromGen = FromGenerator(cfg, tables, ioMap, cfg.maxSelectDepth)
        val (ioInput, output) = ioMap[s]!!
        val (from, input) = keep { s.from to ioInput } mutate { s.from?.let { fromGen.mutate(it) to ioInput } ?: fromGen.from() }
        val exprGen = ExprGenerator(cfg, input)
        val groupBy = keep { s.groupBy } mutate {
            if (nextBoolean(0.8)) listOf(1..3) { TableColumn(column = oneOf(output).name) }
            else listOf(1..3) { exprGen.expr() }
        }
        val select = Select(
            flag = s.flag,
            resultColumns = s.resultColumns,
            from = from,
            where = keep { s.where } mutate { s.where?.let { exprGen.mutate(it) } },
            groupBy = groupBy,
            having = if (groupBy != null) s.having?.let { exprGen.mutate(it) } else null,
            orderBy = s.orderBy,
            limit = s.limit
        )
        ioMap[select] = input to output
        return select
    }
}