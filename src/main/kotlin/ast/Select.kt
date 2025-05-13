package net.sebyte.ast

class Select(
    val flag: Flag? = null,
    val resultColumns: ResultColumns,
    val from: From? = null,
    val where: Expr? = null,
    val groupBy: List<Expr>? = null,
    val having: Expr? = null,
    val orderBy: List<OrderingTerm>? = null,
    val limit: Limit? = null
) : Node {
    enum class Flag {
        DISTINCT, ALL
    }

    override fun toString() = toString(true)

    fun toString(withSemi: Boolean) = buildString {
        append("SELECT")
        if (flag != null) append(" $flag")
        append(" ")
        append(resultColumns)
        if (from != null) append(" FROM $from")
        if (where != null) append(" WHERE $where")
        if (groupBy != null && groupBy.isNotEmpty()) {
            append(" GROUP BY ")
            append(groupBy.joinToString())
        }
        if (having != null) append(" HAVING $having")
        if (orderBy != null && orderBy.isNotEmpty()) {
            append(" ORDER BY ")
            append(orderBy.joinToString())
        }
        if (limit != null) append(" LIMIT $limit")
        if (withSemi) append(";")
    }
}

sealed interface ResultColumns : Node {
    class ResultExpr(
        val expr: Expr,
        val alias: String? = null
    ) : Node {
        override fun toString() = buildString {
            append(expr)
            if (alias != null) append(" AS $alias")
        }
    }

    class ExprList(val exprs: List<ResultExpr>) : ResultColumns {
        override fun toString() = exprs.joinToString()
    }

    object Star : ResultColumns {
        override fun toString() = "*"
    }

    class TableStar(
        val table: String
    ) : ResultColumns {
        override fun toString() = "$table.*"
    }
}

class OrderingTerm(
    val expr: Expr,
    val collateName: String? = null,
    val direction: Direction? = null,
    val nulls: Nulls? = null
) : Node {
    enum class Direction { ASC, DESC }
    enum class Nulls { FIRST, LAST }

    override fun toString() = buildString {
        append(expr)
        if (collateName != null) append(" COLLATE $collateName")
        if (direction != null) append(" $direction")
        if (nulls != null) append(" NULLS $nulls")
    }
}

class Limit(
    val expr: Expr,
    val offset: Expr? = null
) : Node {
    override fun toString() = buildString {
        append(expr)
        if (offset != null) append(" OFFSET $offset")
    }
}