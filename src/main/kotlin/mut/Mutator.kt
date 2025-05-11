package net.sebyte.mut

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig
import net.sebyte.gen.*

class Mutator(
    cfg: GeneratorConfig,
    private val tables: Tables
) : Generator(cfg) {

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
        is TableOrSubquery.Table -> keep { t } mutate { tableOrSubquery() }
        is TableOrSubquery.Subquery -> keep {
            TableOrSubquery.Subquery(
                mutate(t.select, outMap)
            )
        } mutate { tableOrSubquery() }
    }

    fun FromGenerator.mutate(j: JoinClause.JoinedClause) = JoinClause.JoinedClause(
        operator = keep { j.operator } mutate { oneOf(cfg.supportedJoinOperators) },
        tableOrSubquery = keep { mutate(j.tableOrSubquery) } mutate { tableOrSubquery() },
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
        } mutate { from() }

        is TableOrSubqueries -> keep { TableOrSubqueries(f.tableOrSubqueries.map { mutate(it) }) } mutate { from() }
    }

    fun mutate(s: Select, outMap: OutputMap): Select {
        val fromGen = FromGenerator(cfg, tables, outMap, cfg.maxSelectDepth)
        val output = outMap[s]!!
        val from = keep { s.from } mutate {
            s.from?.let {
                val from = fromGen.mutate(it)
                outMap[from] = outMap[it]!! // todo improve
                from
            } ?: fromGen.from()
        }
        val input = from?.let { outMap[it]!! } ?: emptyList()

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
        outMap[select] = output
        return select
    }
}