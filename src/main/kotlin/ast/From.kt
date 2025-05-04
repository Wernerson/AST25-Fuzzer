package net.sebyte.ast

sealed interface From : Node

class TableOrSubqueries(
    val tableOrSubqueries: List<TableOrSubquery>
) : From {
    override fun toString() = tableOrSubqueries.joinToString()
}

sealed interface TableOrSubquery : Node {
    class Table(
        val schemaName: String? = null,
        val tableName: String,
        val alias: String? = null,
        val indexName: String? = null
    ) : TableOrSubquery {
        override fun toString() = buildString {
            if (schemaName != null) append("$schemaName.")
            append(tableName)
            if (alias != null) append(" AS $alias")
            if (indexName != null) append(" INDEXED BY $indexName")
        }
    }

    class TableFunctionCall(
        val schemaName: String? = null,
        val functionName: String,
        val args: List<Expr>,
        val alias: String? = null,
    ) : TableOrSubquery {
        override fun toString() = buildString {
            if (schemaName != null) append("$schemaName.")
            append(functionName)
            append(args.parentString())
            if (alias != null) append(" AS $alias")
        }
    }

    class Subquery(
        val select: Select,
        val alias: String? = null
    ) : TableOrSubquery {
        override fun toString() = buildString {
            append("($select)")
            if (alias != null) append(" AS $alias")
        }
    }

    class NestedFrom(
        val nested: From
    ) : TableOrSubquery {
        override fun toString() = "($nested)"
    }
}


class JoinClause(
    val tableOrSubquery: TableOrSubquery,
    val joinedClauses: List<JoinedClause>
) : From {
    override fun toString() = buildString {
        append(tableOrSubquery)
        append(" ")
        append(joinedClauses.joinToString(separator = " "))
    }

    class JoinedClause(
        val operator: JoinOperator? = null,
        val tableOrSubquery: TableOrSubquery,
        val constraint: JoinConstraint? = null,
    ) : Node {
        override fun toString() = buildString {
            if (operator != null) append("$operator ")
            append(tableOrSubquery)
            if (constraint != null) append(" $constraint")
        }
    }

    enum class JoinOperator(
        val isNatural: Boolean = false
    ) : Node {
        LEFT, RIGHT, FUll, INNER, CROSS,
        LEFT_OUTER, RIGHT_OUTER, FULL_OUTER,
        NATURAL_LEFT(true), NATURAL_RIGHT(true), NATURAL_FUll(true), NATURAL_INNER(true),
        NATURAL_LEFT_OUTER(true), NATURAL_RIGHT_OUTER(true), NATURAL_FUll_OUTER(true);

        override fun toString() = name.replace('_', ' ') + " JOIN"
    }

    sealed interface JoinConstraint : Node {
        class On(val expr: Expr) : JoinConstraint {
            override fun toString() = "ON $expr"
        }

        class Using(val columnNames: List<String>) : JoinConstraint {
            override fun toString() = "USING " + columnNames.parentString()
        }
    }
}