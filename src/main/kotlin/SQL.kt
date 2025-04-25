package net.sebyte

import kotlin.random.Random
import kotlin.random.nextInt

// https://sqlite.org/lang_select.html
// https://sqlite.org/src/rptview?rn=7

private const val MAX_QUERY_DEPTH = 5
private const val MAX_EXPR_DEPTH = 10

fun createDatabase(rand: Random, dataSources: List<DataSource>) = buildString {
    dataSources.forEach { ds ->
        // create table
        append("CREATE TABLE ${ds.name} (")
        append(ds.columns.joinToString())
        append(");")
        appendLine()

        // create index
        append("CREATE ")
        if (rand.nextBoolean()) append("UNIQUE ")
        append("INDEX i${ds.name} ON ${ds.name} (")
        append(Expr.rand(rand, listOf(ds), noAlias = true))
        // TODO WHERE?
        append(");")
        appendLine()

        // insert data
        append("INSERT INTO ${ds.name} VALUES ")
        append(List(rand.nextInt(1..10)) {
            ds.columns.joinToString(prefix = "(", postfix = ")") { it.rand(rand) }
        }.joinToString())
        append(";")
        appendLine()
    }
}

class Select(
    val resultColumn: List<ResultColumn>,
    val from: From? = null,
    val where: Where? = null,
    val groupBy: GroupBy? = null,
    val having: Expr? = null,
    val orderBy: OrderBy? = null,
    val limit: Limit? = null
) {
    override fun toString() = toString(false)
    fun toString(subquery: Boolean = false) = buildString {
        append("SELECT ")
        append(resultColumn.joinToString(postfix = " "))
        if (from != null) append("FROM $from ")
        if (where != null) append("WHERE $where ")
        if (groupBy != null) append("GROUP BY $groupBy ")
        if (having != null) append("HAVING $having ")
        if (orderBy != null) append("ORDER BY $orderBy ")
        if (limit != null) append("LIMIT $limit ")
        if (!subquery) append(";")
    }

    companion object {
        fun rand(rand: Random, dataSources: List<DataSource>) = rand(rand, dataSources, 0).first.toString()
        fun rand(rand: Random, dataSources: List<DataSource>, depth: Int): Pair<Select, List<DataSource>> {
            val (from, dataSources) = From.rand(rand, dataSources, depth)
            val where = if (rand.nextBoolean()) Where.rand(rand, dataSources) else null

            val resultCols = mutableListOf<ResultColumn>()
            val sources = mutableSetOf<DataSource>()
            for (i in 0..rand.nextInt(0..5)) {
                val (resultColumn, dataSource) = ResultColumn.rand(rand, dataSources)
                resultCols.add(resultColumn)
                sources.add(dataSource)
            }

            val limit = Limit(Expr.LiteralValue("100"))
            return Pair(
                Select(
                    resultColumn = resultCols, from = from, where = where, limit = limit
                ), sources.toList()
            )
        }
    }
}

sealed interface From {

    companion object {
        fun rand(rand: Random, dataSources: List<DataSource>, depth: Int): Pair<From, List<DataSource>> =
            if (rand.nextBoolean()) FromJoin.rand(rand, dataSources, depth)
            else FromTableOrSubquery.rand(rand, dataSources, depth)
    }

    enum class JoinOp {
        LEFT, INNER, CROSS
    }

    class FromJoin(
        val left: TableOrSubquery, val op: JoinOp, val right: TableOrSubquery
    ) : From {

        override fun toString() = " $left $op JOIN $right "

        companion object {
            fun rand(rand: Random, dataSources: List<DataSource>, depth: Int): Pair<From, List<DataSource>> {
                val (left, dsLeft) = TableOrSubquery.rand(rand, dataSources, depth + 1)
                val (right, dsRight) = TableOrSubquery.rand(rand, dataSources, depth + 1)

                val op = JoinOp.entries.random(rand)
                return Pair(FromJoin(left, op, right), listOf(dsLeft, dsRight))
            }
        }
    }

    class FromTableOrSubquery(
        val tableOrSubquery: List<TableOrSubquery>,
    ) : From {
        override fun toString() = tableOrSubquery.joinToString()

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, depth: Int
            ): Pair<FromTableOrSubquery, List<DataSource>> {
                val newSources = mutableSetOf<DataSource>()
                val subs = mutableListOf<TableOrSubquery>()
                for (i in 0..rand.nextInt(0..3)) {
                    val (sub, dataSources) = TableOrSubquery.rand(rand, dataSources, depth)
                    newSources.add(dataSources)
                    subs.add(sub)
                }
                return Pair(FromTableOrSubquery(subs), newSources.toList())
            }
        }
    }

    sealed interface TableOrSubquery {
        class Table(
            val table: String, val alias: String
        ) : TableOrSubquery {
            override fun toString() = "$table AS $alias"
        }

        class Subquery(
            val select: Select,
            val alias: String,
        ) : TableOrSubquery {
            override fun toString() = "(${select.toString(subquery = true)})  AS $alias"
        }

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, depth: Int
            ): Pair<TableOrSubquery, DataSource> {
                val max = if (depth >= MAX_QUERY_DEPTH) 1 else 2
                return when (rand.nextInt(1..max)) {
                    1 -> {
                        val source = dataSources.random(rand)
                        val ai = rand.nextInt(1000..9999)
                        val alias = "ta$ai"
                        Pair(Table(source.name, alias), DataSource(alias, source.columns))
                    }

                    2 -> {
                        val (select, dataSources) = Select.rand(rand, dataSources, depth + 1)
                        val ai = rand.nextInt(1000..9999)
                        val alias = "sa$ai"
                        Pair(Subquery(select, alias), DataSource(alias, dataSources.flatMap { it.columns }))
                    }

                    else -> error("Random number out of bounds!")
                }
            }
        }
    }
}

sealed interface ResultColumn {
    class TableColumn(
        val table: String, val column: String, val alias: String
    ) : ResultColumn {
        override fun toString() = "$table.$column AS $alias"
    }


    companion object {
        fun rand(
            rand: Random, dataSources: List<DataSource>
        ): Pair<ResultColumn, DataSource> {
            val ds = dataSources.random(rand)
            val col = ds.columns.random(rand)
            val ai = rand.nextInt(0..9999)
            val alias = DataColumn(
                "ca$ai", col.type, col.nullable
            )
            return Pair(TableColumn(ds.name, col.name, alias.name), DataSource(ds.name, listOf(alias)))
        }
    }
}

private val BINARY_OPS = listOf(
//    "||", "->", "->>",
    "*", "/", "%", "+", "-", "&", "|", "<<", ">>", "<", ">", "<=", ">=", "=", "==", "<>", "!=", "IS", "IS NOT", "IN",
//    "MATCH",
    "LIKE", "AND", "OR"
)

private val UNARY_OPS = listOf(
//    "# COLLATE",
    "# ISNULL", " # NOTNULL", "# NOT NULL", "NOT #", "-(#)", "+#", "~#"
)

private val TERNARY_OPS = listOf(
    "#1 BETWEEN #2 AND #3",
)

private val FUNCTIONS = listOf(
    "abs" to 1, "changes" to 0, "hex" to 1, "ifnull" to 2, "length" to 1
)

sealed interface Expr {
    class LiteralValue(val literal: String) : Expr {
        override fun toString() = literal

        companion object {
            fun rand(rand: Random) = LiteralValue(DataType.entries.random(rand).rand(rand))
        }
    }

    class TableColumn(
        val table: String? = null, val column: String
    ) : Expr {
        override fun toString() = buildString {
            if (table != null) append("$table.")
            append(column)
        }

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, noAlias: Boolean = false
            ) = dataSources.randomOrNull(rand)?.let { t ->
                TableColumn(
                    if (noAlias) null else t.name, t.columns.random(rand).name
                )
            }
        }
    }

    class FunctionCall(
        val function: String, val args: List<Expr>
    ) : Expr {
        override fun toString() = "$function(${args.joinToString()})"

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, depth: Int, noAlias: Boolean
            ) = FUNCTIONS.random(rand).let { (func, args) ->
                FunctionCall(func, List(args) { Expr.rand(rand, dataSources, depth, noAlias) })
            }
        }
    }

    class UnaryExpr(
        val operator: String, val operand: Expr
    ) : Expr {
        override fun toString() = operator.replace("#", operand.toString())

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, depth: Int, noAlias: Boolean
            ): UnaryExpr = UnaryExpr(UNARY_OPS.random(rand), Expr.rand(rand, dataSources, depth, noAlias))
        }
    }

    class BinaryExpr(
        val left: Expr, val operator: String, val right: Expr
    ) : Expr {
        override fun toString() = "($left $operator $right)"

        companion object {
            fun rand(
                rand: Random, dataSources: List<DataSource>, depth: Int, noAlias: Boolean
            ): BinaryExpr = BinaryExpr(
                Expr.rand(rand, dataSources, depth, noAlias),
                BINARY_OPS.random(rand),
                Expr.rand(rand, dataSources, depth, noAlias)
            )
        }
    }

    companion object {
        fun rand(
            rand: Random, dataSources: List<DataSource>, depth: Int = 0, noAlias: Boolean = false
        ): Expr = if (depth < MAX_EXPR_DEPTH) when (rand.nextInt(1..5)) {
            1 -> UnaryExpr.rand(rand, dataSources, depth + 1, noAlias)
            2 -> BinaryExpr.rand(rand, dataSources, depth + 1, noAlias)
            3 -> LiteralValue.rand(rand)
            4 -> FunctionCall.rand(rand, dataSources, depth + 1, noAlias)
            5 -> TableColumn.rand(rand, dataSources, noAlias) ?: LiteralValue.rand(rand)
            else -> error("Random number out of bounds!")
        } else TableColumn.rand(rand, dataSources, noAlias) ?: LiteralValue.rand(rand)
    }
}

class Where(val expr: Expr) {

    override fun toString() = "$expr"

    companion object {
        fun rand(rand: Random, dataSources: List<DataSource>): Where = Where(
            Expr.rand(rand, dataSources)
        )
    }
}

class GroupBy(val expr: List<Expr>)
class OrderBy(val orderingTerms: List<String>)

class Limit(
    val expr: Expr, val offset: Expr? = null
) {
    override fun toString() = buildString {
        append(expr.toString())
        if (offset != null) append(" OFFSET $offset")
    }

    companion object {
        fun rand(rand: Random, dataSources: List<DataSource>) =
            if (rand.nextBoolean()) Limit(Expr.rand(rand, dataSources), Expr.rand(rand, dataSources))
            else Limit(Expr.rand(rand, dataSources))
    }
}