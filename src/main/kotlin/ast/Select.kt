package net.sebyte.ast

fun main() {
    println(Select(
        flag = Select.Flag.DISTINCT,
        resultColumns = listOf(
            ResultColumn.Star
        ),
        from = TableOrSubqueries(listOf(TableOrSubquery.Table(tableName = "t1"))),
        where = BinaryExpr(
            FunctionCall("abs", listOf(LiteralValue.NumericLiteral(1))),
            BinaryExpr.Op.EQUAL2,
            TableColumn(table = "t1", column = "c1")
        ),
        orderBy = listOf(OrderingTerm(TableColumn(table = "t1", column = "c2")))
    ))
}

class Select(
    private val flag: Flag? = null,
    private val resultColumns: List<ResultColumn>,
    private val from: From? = null,
    private val where: Expr? = null,
    private val groupBy: List<Expr>? = null,
    private val having: Expr? = null,
    // TODO window
    // todo compound
    private val orderBy: List<OrderingTerm>? = null,
    private val limit: Limit? = null
) : Node {
    enum class Flag {
        DISTINCT, ALL
    }

    override fun toString() = buildString {
        append("SELECT")
        if (flag != null) append(" $flag")
        append(" ")
        append(resultColumns.joinToString())
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
        append(";")
    }
}

sealed interface ResultColumn : Node {
    class Expr(
        private val expr: Expr,
        private val alias: String? = null
    ) : ResultColumn {
        override fun toString() = buildString {
            append(expr)
            if (alias != null) append(" AS $alias")
        }
    }

    object Star : ResultColumn {
        override fun toString() = "*"
    }

    class TableStar(
        private val table: String
    ) : ResultColumn {
        override fun toString() = "$table.*"
    }
}

class OrderingTerm(
    private val expr: Expr,
    private val collateName: String? = null,
    private val direction: Direction? = null,
    private val nulls: Nulls? = null
): Node {
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
    private val expr: Expr,
    private val offset: Expr? = null
): Node {
    override fun toString() = buildString {
        append(expr)
        if (offset != null) append(" OFFSET $offset")
    }
}