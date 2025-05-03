package net.sebyte.ast

sealed interface From : Node

class TableOrSubqueries(
    private val tableOrSubqueries: List<TableOrSubquery>
) : From {
    override fun toString() = tableOrSubqueries.joinToString()
}

sealed interface TableOrSubquery : Node {
    class Table(
        private val schemaName: String? = null,
        private val tableName: String,
        private val alias: String? = null,
        private val indexName: String? = null
    ) : TableOrSubquery {
        override fun toString() = buildString {
            if (schemaName != null) append("$schemaName.")
            append(tableName)
            if (alias != null) append(" AS $alias")
            if (indexName != null) append(" INDEXED BY $indexName")
        }
    }

    class TableFunctionCall(
        private val schemaName: String? = null,
        private val functionName: String,
        private val args: List<Expr>,
        private val alias: String? = null,
    ) : TableOrSubquery {
        override fun toString() = buildString {
            if (schemaName != null) append("$schemaName.")
            append(functionName)
            append(args.parentString())
            if (alias != null) append(" AS $alias")
        }
    }

    class Subquery(
        private val select: Select,
        private val alias: String? = null
    ) : TableOrSubquery {
        override fun toString() = buildString {
            append("($select)")
            if (alias != null) append(" AS $alias")
        }
    }

    class Values(
        private val exprs: List<Expr>,
        private val alias: String? = null
    ): TableOrSubquery {
        override fun toString() = buildString {
            append(exprs.parentString())
            if (alias != null) append(" AS $alias")
        }
    }

    class NestedFrom(
        private val nested: From
    ) : TableOrSubquery {
        override fun toString() = "($nested)"
    }
}


class JoinClause(
    private val tableOrSubquery: TableOrSubquery,
    private val joinedClauses: List<JoinedClause>
) : From {
    override fun toString() = TODO()

    class JoinedClause(
        private val joinOperator: JoinClause.JoinOperator? = null,
        private val tableOrSubquery: TableOrSubquery,
        private val joinConstraint: JoinClause.JoinConstraint? = null,
    ) : Node {
        override fun toString() = TODO()
    }

    enum class JoinOperator : Node {
        LEFT, RIGHT, FUll, INNER, CROSS,
        LEFT_OUTER, RIGHT_OUTER, FULL_OUTER,
        NATURAL_LEFT, NATURAL_RIGHT, NATURAL_FUll, NATURAL_INNER,
        NATURAL_LEFT_OUTER, NATURAL_RIGHT_OUTER, NATURAL_FUll_OUTER;

        override fun toString() = name.replace('_', ' ')
    }

    sealed interface JoinConstraint : Node {
        class On(private val expr: Expr) : JoinConstraint {
            override fun toString() = "ON $expr"
        }

        class Using(private val columnNames: List<String>) : JoinConstraint {
            override fun toString() = "USING " + columnNames.parentString()
        }
    }
}