package net.sebyte.ast

sealed interface Expr : Node

sealed interface LiteralValue : Expr {

    class NumericLiteral(private val value: String) : LiteralValue {
        constructor(value: Int) : this("$value")
        constructor(value: Double) : this("$value")

        override fun toString() = value
    }

    class StringLiteral(private val value: String) : LiteralValue {
        override fun toString() = value
    }

    class BlobLiteral(private val value: String) : LiteralValue {
        @OptIn(ExperimentalStdlibApi::class)
        constructor(bytes: ByteArray) : this("X'${bytes.toHexString()}'")

        override fun toString() = value
    }

    enum class Constants : LiteralValue {
        NULL, TRUE, FALSE,
        CURRENT_TIME, CURRENT_DATE,
        CURRENT_TIMESTAMP
    }
}

// todo bind-parameter

class TableColumn(
    private val schema: String? = null,
    private val table: String? = null,
    private val column: String
) : Expr {
    override fun toString() = buildString {
        if (schema != null) append("$schema.")
        if (table != null) append("$table.")
        append(column)
    }
}

class UnaryExpr(
    private val op: Op,
    private val expr: Expr
) : Expr {
    enum class Op(
        op: String? = null,
        private val opLeft: Boolean = true
    ) {
        TILDE("~"), PLUS("+"), MINUS("-"),
        NOT, ISNULL, NOTNULL, NOT_NULL;

        private val op = op ?: name.replace('_', ' ')

        fun toString(expr: Expr) = if (opLeft) "$op$expr" else "$expr$op"
    }

    override fun toString() = op.toString(expr)
}

class BinaryExpr(
    private val left: Expr,
    private val op: Op,
    private val right: Expr
) : Expr {
    enum class Op(private val op: String? = null) {
        CONCAT("||"), EXTRACT("->"), EXTRACT2("->>"),
        TIMES("*"), DIV("/"), MOD("%"),
        PLUS("+"), MINUS("-"),
        BAND("&"), BOR("|"), LSHIFT("<<"), RSHIFT(">>"),
        SMALLER("<"), SMALLER_EQUAL("<="), GREATER(">"), GREATER_EQUAL(">="),
        EQUAL("="), EQUAL2("=="), NOT_EQUAL("<>"), NOT_EQUAL2("!="),
        IS, IS_NOT, IS_DISTINCT_FROM, IS_NOT_DISTINCT_FROM,
        AND, OR;

        override fun toString() = op ?: name.replace('_', ' ')
    }

    override fun toString() = "$left$op$right"
}

class FunctionCall(
    private val name: String,
    private val args: List<Expr> // todo function args
    // todo filter, over clause
) : Expr {
    override fun toString() = "$name${args.parentString()}"
}

class Tuple(
    private val exprs: List<Expr>
) : Expr {
    override fun toString() = exprs.parentString()
}

class Cast(
    private val expr: Expr,
    private val typeName: String // todo
) : Expr {
    override fun toString() = "CAST ($expr as $typeName)"
}

class Collate(
    private val expr: Expr,
    private val collationName: String
) : Expr {
    override fun toString() = "$expr COLLATE $collationName"
}

// todo between, in, exists, case, regexp, etc.