package net.sebyte.ast

sealed interface From : Node

class TableOrSubqueries(
    val tableOrSubqueries: List<TableOrSubquery>
) : From {
    override fun toString() = tableOrSubqueries.joinToString()
}

sealed interface TableOrSubquery : Node {
    val alias: String?
    fun aliased(alias: String): TableOrSubquery

    data class Table(
        val schemaName: String? = null,
        val tableName: String,
        val indexName: String? = null,
        override val alias: String? = null
    ) : TableOrSubquery {
        override fun aliased(alias: String) = copy(alias=alias)
        override fun toString() = buildString {
            if (schemaName != null) append("$schemaName.")
            append(tableName)
            if (alias != null) append(" AS $alias")
            if (indexName != null) append(" INDEXED BY $indexName")
        }
    }

    data class Subquery(
        val select: Select,
        override val  alias: String? = null
    ) : TableOrSubquery {
        override fun aliased(alias: String) = copy(alias=alias)
        override fun toString() = buildString {
            append("(${select.toString(false)})")
            if (alias != null) append(" AS $alias")
        }
    }

//    data class TableFunctionCall(
//        val schemaName: String? = null,
//        val functionName: String,
//        val args: List<Expr>,
//        override val alias: String? = null,
//    ) : TableOrSubquery {
//        override fun aliased(alias: String) = copy(alias=alias)
//        override fun toString() = buildString {
//            if (schemaName != null) append("$schemaName.")
//            append(functionName)
//            append(args.parentString())
//            if (alias != null) append(" AS $alias")
//        }
//    }
//
//    data class NestedFrom(
//        val nested: From,
//        override val alias: String? = null,
//    ) : TableOrSubquery {
//        override fun aliased(alias: String) = copy(alias=alias)
//        override fun toString() = buildString {
//            append("($nested)")
//            if (alias != null) append(" AS $alias")
//        }
//    }
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