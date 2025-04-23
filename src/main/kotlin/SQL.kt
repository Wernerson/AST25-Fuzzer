package net.sebyte

import kotlin.random.Random
import kotlin.random.nextInt

// https://sqlite.org/lang_select.html

private const val MAX_QUERY_DEPTH = 3
private const val MAX_EXPR_DEPTH = 5

fun createDatabase(rand: Random, dataSources: List<DataSource>) = buildString {
    dataSources.forEach { ds ->
        // create table
        append("CREATE TABLE ${ds.name} (")
        append(ds.columns.joinToString())
        append(");")
        appendLine()

        // create index

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
        fun rand(
            rand: Random,
            dataSources: List<DataSource>,
            depth: Int = 0
        ): Pair<Select, List<DataSource>> {
            val (from, dataSources) = From.rand(rand, dataSources, depth)

            val where = if (rand.nextBoolean()) Where.rand(rand, dataSources) else null

            val resultCols = mutableListOf<ResultColumn>()
            val sources = mutableSetOf<DataSource>()
            for (i in 0..rand.nextInt(0..5)) {
                val (resultColumn, dataSource) = ResultColumn.rand(rand, dataSources)
                resultCols.add(resultColumn)
                sources.add(dataSource)
            }
            return Pair(
                Select(
                    resultColumn = resultCols,
                    from = from,
                    where = where,
                ), sources.toList()
            )
        }
    }
}

class From(
    val tableOrSubquery: List<TableOrSubquery>,
) {
    override fun toString() = tableOrSubquery.joinToString()

    companion object {
        fun rand(
            rand: Random, dataSources: List<DataSource>, depth: Int
        ): Pair<From, List<DataSource>> {
            val newSources = mutableSetOf<DataSource>()
            val subs = mutableListOf<TableOrSubquery>()
            for (i in 0..rand.nextInt(0..3)) {
                val (sub, dataSources) = TableOrSubquery.rand(rand, dataSources, depth)
                newSources.add(dataSources)
                subs.add(sub)
            }
            return Pair(From(subs), newSources.toList())
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

sealed interface ResultColumn {
//    object Star : ResultColumn {
//        override fun toString() = "*"
//    }

//    class TableStar(val table: String) : ResultColumn {
//        override fun toString() = "$table.*"
//    }

    class TableColumn(
        val table: String, val column: String, val alias: String
    ) : ResultColumn {
        override fun toString() = "$table.$column AS $alias"
    }

//    class ResultExpr(
//        val expr: Expr, val alias: String? = null
//    ) : ResultColumn {
//        override fun toString() = buildString {
//            append(expr)
//            if (alias != null) append(" AS $alias") // todo randomize if AS should be there?
//        }
//    }

    companion object {
        fun rand(
            rand: Random, dataSources: List<DataSource>
        ): Pair<ResultColumn, DataSource> {
//            return when (rand.nextInt(1..2)) {
//                1 -> Star
//                1 -> TableStar(TABLES.random(rand))
//                2 -> ResultExpr(Expr.rand(rand)) // todo alias?
//                else -> error("Random number out of bounds!")
//            }
            val ds = dataSources.random(rand)
            val col = ds.columns.random(rand)
            val ai = rand.nextInt(0..9999)
            val alias = DataColumn(
                "ca$ai",
                col.type,
                col.nullable
            )
            return Pair(TableColumn(ds.name, col.name, alias.name), DataSource(ds.name, listOf(alias)))
        }
    }
}

sealed interface Expr {
    class LiteralValue(val literal: String) : Expr {
        override fun toString() = literal
    }

    class Column(
        val table: String,
        val column: String
    ) : Expr {
        override fun toString() = "$table.$column"
    }

    enum class BinaryOp(private val op: String) {
        AND("AND"), OR("OR"),
        EQ("="), NE("<>"),
        LT("<"), LE("<="),
        GT(">"), GE(">="),
        ADD("+"), SUB("-"),
        MUL("*"), DIV("/");

        override fun toString() = op
    }

    class BinaryExpr(
        val left: Expr,
        val operator: BinaryOp,
        val right: Expr
    ) : Expr {
        override fun toString() = "($left $operator $right)"
    }

    companion object {
        fun randValue(
            rand: Random,
            dataSources: List<DataSource>,
            dataType: DataType,
            depth: Int
        ): Expr = if (depth >= MAX_EXPR_DEPTH || rand.nextBoolean()) LiteralValue(dataType.rand(rand))
        else dataSources
            .flatMap { t -> t.columns.map { Pair(t.name, it) } }
            .filter { (_, c) -> c.type == dataType }
            .randomOrNull(rand)
            ?.let { (t, c) -> Column(t, c.name) }
            ?: LiteralValue(dataType.rand(rand))


        fun randBoolean(
            rand: Random,
            dataSources: List<DataSource>,
            depth: Int = 0
        ): Expr  {
            val type = dataSources.flatMap { it.columns }.random(rand).type
            val left = randValue(rand, dataSources, type, depth+1)
            val right = randValue(rand, dataSources, type, depth+1)
            val op = listOf(BinaryOp.EQ, BinaryOp.NE).random(rand)
            return BinaryExpr(left, op, right)
        }
    }
}

class Where(val expr: Expr) {

    override fun toString() = "$expr"

    companion object {
        fun rand(rand: Random, dataSources: List<DataSource>): Where  = Where(
            Expr.randBoolean(rand, dataSources)
        )
    }
}

class GroupBy(val expr: List<Expr>)
class OrderBy(val orderingTerms: List<String>)
class Limit(val expr: Expr)