package net.sebyte.ast

enum class DataType {
    INTEGER, REAL, TEXT, BLOB
}

sealed interface Expr : Node

sealed interface LiteralValue : Expr {

    class NumericLiteral(val value: String) : LiteralValue {
        constructor(value: Int) : this("$value")
        constructor(value: Double) : this("$value")

        override fun toString() = value
    }

    class StringLiteral(val value: String) : LiteralValue {
        override fun toString() = value
    }

    class BlobLiteral(val value: String) : LiteralValue {
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
    val schema: String? = null,
    val table: String? = null,
    val column: String
) : Expr {
    override fun toString() = buildString {
        if (schema != null) append("$schema.")
        if (table != null) append("$table.")
        append(column)
    }
}

class UnaryExpr(
    val op: Op,
    val expr: Expr
) : Expr {
    enum class Op(
        op: String? = null,
        val opLeft: Boolean = true
    ) {
        TILDE("~"), PLUS("+"), MINUS("-"),
        NOT, ISNULL, NOTNULL, NOT_NULL;

        val op = op ?: name.replace('_', ' ')

        fun toString(expr: Expr) = if (opLeft) "$op $expr" else "$expr $op"
    }

    override fun toString() = op.toString(expr)
}

class BinaryExpr(
    val left: Expr,
    val op: Op,
    val right: Expr
) : Expr {
    enum class Op(val op: String? = null) {
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

    override fun toString() = "$left $op $right"
}

class FunctionCall(
    val name: String,
    val args: List<Expr> // todo function args
    // todo filter, over clause
) : Expr {
    override fun toString() = "$name${args.parentString()}"
}

class Tuple(
    val exprs: List<Expr>
) : Expr {
    override fun toString() = exprs.parentString()
}

class Cast(
    val expr: Expr,
    val typeName: String // todo
) : Expr {
    override fun toString() = "CAST ($expr as $typeName)"
}

class Collate(
    val expr: Expr,
    val collationName: String
) : Expr {
    override fun toString() = "$expr COLLATE $collationName"
}

// todo between, in, exists, case, regexp, etc.