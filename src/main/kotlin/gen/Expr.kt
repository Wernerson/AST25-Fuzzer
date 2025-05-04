package net.sebyte.gen

import net.sebyte.ast.*
import kotlin.random.Random

private val ALPHABET = ('a'..'z').toList() + ('A'..'Z').toList()

private data class Function(
    val name: String,
    val parameters: List<DataType>,
    val returnType: DataType,
    val deterministic: Boolean = true
)

sealed interface DataEntry {
    data class ScopedColumn(
        val scope: String,
        val name: String,
    ) : DataEntry

    data class Column(val name: String) : DataEntry
}

typealias DataSet = List<DataEntry>

class ExprGenerator(
    r: Random,
    private val input: DataSet,
    private val depth: Int = 5,
    private val onlyDeterministic: Boolean = false
) : Generator(r) {

    fun with(
        depth: Int = this.depth,
        input: DataSet = this.input,
        onlyDeterministic: Boolean = this.onlyDeterministic
    ) = ExprGenerator(r, input, depth, onlyDeterministic)

    companion object {
        fun constExprGenerator(r: Random) = ExprGenerator(r, emptyList())
    }

    fun expr(): Expr = oneOf {
        add(::literalValue)
        if (input.isNotEmpty()) add(::tableColumn)
        if (depth > 0) {
            add(::unaryExpr)
            add(::binaryExpr)
            add(::functionCall)
            add { Tuple(listOf(with(depth - 1).expr())) }
        }
        // todo tuple, cast collate
    }

    fun exprOrNull(nullPct: Double) =
        if (nextBoolean(nullPct)) null
        else expr()

    fun literalValue(): LiteralValue = oneOf {
        add { LiteralValue.NumericLiteral(r.nextInt()) }
        add { LiteralValue.BlobLiteral(r.nextBytes(10)) }
        add {
            LiteralValue.StringLiteral(
                listOf(5..10, ALPHABET).joinToString("")
            )
        }

        addAll(LiteralValue.Constants.entries.map { { it } })
        if (!onlyDeterministic) addAll(LiteralValue.Variables.entries.map { { it } })
    }

    fun tableColumn(): TableColumn = oneOf(
        input.map {
            when (it) {
                is DataEntry.ScopedColumn -> TableColumn(table = it.scope, column = it.name)
                is DataEntry.Column -> TableColumn(column = it.name)
            }
        }
    )

    fun unaryExpr(): UnaryExpr = UnaryExpr(
        oneOf(UnaryExpr.Op.entries), with(depth - 1).expr()
    )

    fun binaryExpr(): BinaryExpr = BinaryExpr(
        with(depth - 1).expr(), oneOf(BinaryExpr.Op.entries), with(depth - 1).expr()
    )

    fun functionCall(): FunctionCall = listOf(
        Function("abs", listOf(DataType.REAL), DataType.REAL),
        Function("abs", listOf(DataType.INTEGER), DataType.INTEGER),
        Function("changes", listOf(), DataType.INTEGER, false),
        Function("char", listOf(3..10) { DataType.INTEGER }, DataType.TEXT),
        Function("coalesce", listOf(2..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("concat", listOf(1..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("concat_ws", listOf(2..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("format", listOf(1..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("glob", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
        Function("hex", listOf(DataType.BLOB), DataType.TEXT),
        Function("ifnull", listOf(2..2) { oneOf(DataType.entries) }, DataType.INTEGER),
//        Function("iif", listOf(DataType.INTEGER, DataType.INTEGER), DataType.INTEGER),
//        Function("if", listOf(DataType.INTEGER, DataType.INTEGER), DataType.INTEGER),
        Function("instr", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
        Function("last_insert_rowid", listOf(), DataType.INTEGER, false),
        Function("length", listOf(DataType.TEXT), DataType.INTEGER),
        Function("like", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
//        Function("like", listOf(DataType.TEXT, DataType.TEXT, DataType.TEXT), DataType.INTEGER),
//        Function("likelihood", listOf(DataType.TEXT), DataType.INTEGER),
        Function("likely", listOf(1..1) { oneOf(DataType.entries) }, DataType.INTEGER, false),
//        Function("load_extension", listOf(1..2, DataType.TEXT), DataType.INTEGER, false),
        Function("lower", listOf(DataType.TEXT), DataType.TEXT),
        Function("ltrim", listOf(1..2, DataType.TEXT), DataType.TEXT),
        Function("max", listOf(2..5, DataType.INTEGER), DataType.INTEGER),
        Function("max", listOf(2..5, DataType.REAL), DataType.REAL),
        Function("min", listOf(2..5, DataType.INTEGER), DataType.INTEGER),
        Function("min", listOf(2..5, DataType.REAL), DataType.REAL),
        Function("nullif", listOf(2..2) { oneOf(DataType.entries) }, DataType.INTEGER),
        Function("octet_length", listOf(DataType.TEXT), DataType.INTEGER),
        Function("printf", listOf(1..5, DataType.TEXT), DataType.TEXT),
        Function("quote", listOf(DataType.TEXT), DataType.TEXT),
        Function("random", listOf(), DataType.INTEGER, false),
        Function("randomblob", listOf(DataType.INTEGER), DataType.BLOB, false),
        Function("replace", listOf(DataType.TEXT, DataType.TEXT, DataType.TEXT), DataType.TEXT),
        Function("round", listOf(DataType.REAL), DataType.REAL),
        Function("round", listOf(DataType.REAL, DataType.INTEGER), DataType.REAL),
        Function("rtrim", listOf(1..2, DataType.TEXT), DataType.TEXT),
        Function("sign", listOf(1..1) { oneOf(DataType.INTEGER, DataType.REAL) }, DataType.INTEGER),
//        Function("soundex", listOf(DataType.TEXT), DataType.TEXT),
        Function("sqlite_source_id", listOf(), DataType.TEXT, false),
        Function("sqlite_version", listOf(), DataType.TEXT, false),
        Function("substr", listOf(DataType.TEXT, DataType.INTEGER, DataType.INTEGER), DataType.TEXT),
        Function("substring", listOf(DataType.TEXT, DataType.INTEGER, DataType.INTEGER), DataType.TEXT),
        Function("total_changes", listOf(), DataType.INTEGER, false),
        Function("trim", listOf(1..2, DataType.TEXT), DataType.TEXT),
        Function("typeof", listOf(1..1) { oneOf(DataType.entries) }, DataType.TEXT),
//        Function("unhex", listOf(1..2, DataType.TEXT), DataType.BLOB),
        Function("unicode", listOf(DataType.TEXT), DataType.INTEGER),
        Function("unlikely", listOf(1..1) { oneOf(DataType.entries) }, DataType.INTEGER, false),
        Function("upper", listOf(DataType.TEXT), DataType.TEXT),
        Function("zeroblob", listOf(DataType.INTEGER), DataType.BLOB),
    ).filter { !onlyDeterministic || it.deterministic }
        .let { oneOf(it) }
        .let { (name, params) ->
            val exprGen = with(depth - 1)
            val args = params.map { exprGen.expr() } // todo type it
            FunctionCall(name, args)
        }
}