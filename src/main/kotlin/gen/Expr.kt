package net.sebyte.gen

import net.sebyte.ast.*
import net.sebyte.cfg.GeneratorConfig
import kotlin.random.nextInt

private val ALPHABET = ('a'..'z').toList() + ('A'..'Z').toList()

private data class Function(
    val name: String,
    val parameters: List<DataType>,
    val returnType: DataType,
    val deterministic: Boolean = true
)

data class DataEntry(
    val scope: String?,
    val name: String,
    val type: DataType
)

data class ExprType(
    val allowedTypes: List<DataType>,
    val nullable: Boolean
) {
    companion object {
        val ANY = ExprType(DataType.entries, true)
        val INTEGER = ExprType(listOf(DataType.INTEGER), false)
    }

    fun allows(dataType: DataType) = dataType in allowedTypes
}

typealias DataSet = List<DataEntry>

class ExprGenerator(
    cfg: GeneratorConfig,
    private val input: DataSet,
    private val depth: Int = cfg.maxExprDepth,
    private val onlyDeterministic: Boolean = true,
    private val exprType: ExprType = ExprType.ANY
) : Generator(cfg) {

    fun with(
        depth: Int = this.depth,
        input: DataSet = this.input,
        onlyDeterministic: Boolean = this.onlyDeterministic,
        exprType: ExprType = this.exprType
    ) = ExprGenerator(cfg, input, depth, onlyDeterministic, exprType)

    companion object {
        fun constExprGenerator(cfg: GeneratorConfig) = ExprGenerator(cfg, emptyList())
    }

    fun expr(): Expr = oneOf {
        add(::literalValue)
        if (input.isNotEmpty()) add(::tableColumn)
        if (depth > 0) {
            if (exprType.allows(DataType.INTEGER) || exprType.allows(DataType.REAL)) {
                add(::unaryExpr)
                add(::binaryExpr)
            }
            add(::functionCall)
        }
    }

    fun exprOrNull(nonNullPct: Double = 0.5) =
        if (nextBoolean(nonNullPct)) expr()
        else null

    fun literalValue(): LiteralValue = oneOf {
        if (exprType.allows(DataType.INTEGER)) {
            add { LiteralValue.NumericLiteral(r.nextInt(cfg.literalIntRange)) }
            addAll(
                listOf(
                    { LiteralValue.Constants.TRUE },
                    { LiteralValue.Constants.FALSE },
                )
            )
        }
        if (exprType.allows(DataType.REAL)) add { LiteralValue.NumericLiteral(r.nextDouble()) }
        if (exprType.allows(DataType.BLOB)) add { LiteralValue.BlobLiteral(r.nextBytes(cfg.literalBlobSize)) }
        if (exprType.allows(DataType.TEXT)) {
            if (!onlyDeterministic) addAll(LiteralValue.Variables.entries.map { { it } })
            add {
                LiteralValue.StringLiteral(
                    listOf(cfg.literalTextSizeRange, ALPHABET).joinToString("")
                )
            }
        }
        if (exprType.nullable) add { LiteralValue.Constants.NULL }
    }

    fun tableColumn(): TableColumn = oneOf(input).let { TableColumn(table = it.scope, column = it.name) }

    fun unaryExpr(): UnaryExpr = UnaryExpr(
        oneOf(UnaryExpr.Op.entries), with(depth - 1).expr()
    )

    fun binaryExpr(): BinaryExpr = BinaryExpr(
        with(depth - 1).expr(), oneOf(cfg.supportedBinaryOps), with(depth - 1).expr()
    )

    fun functionCall(): FunctionCall = listOf(
        Function("abs", listOf(DataType.REAL), DataType.REAL),
        Function("abs", listOf(DataType.INTEGER), DataType.INTEGER),
        Function("changes", listOf(), DataType.INTEGER, false),
        Function("char", listOf(3..10) { DataType.INTEGER }, DataType.TEXT),
        Function("coalesce", listOf(2..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("concat", listOf(1..10) { oneOf(DataType.entries) }, DataType.TEXT),
//        Function("concat_ws", listOf(2..10) { oneOf(DataType.entries) }, DataType.TEXT),
//        Function("format", listOf(1..10) { oneOf(DataType.entries) }, DataType.TEXT),
        Function("glob", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
        Function("hex", listOf(DataType.BLOB), DataType.TEXT),
        Function("ifnull", listOf(2..2) { oneOf(DataType.entries) }, DataType.INTEGER),
//        Function("iif", listOf(DataType.INTEGER, DataType.INTEGER), DataType.INTEGER),
//        Function("if", listOf(DataType.INTEGER, DataType.INTEGER), DataType.INTEGER),
        Function("instr", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
        Function("last_insert_rowid", listOf(), DataType.INTEGER, false),
        Function("length", listOf(DataType.TEXT), DataType.INTEGER),
//        Function("like", listOf(DataType.TEXT, DataType.TEXT), DataType.INTEGER),
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
//        Function("printf", listOf(1..5, DataType.TEXT), DataType.TEXT),
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
    ).filter { cfg.supports(it.name) }
        .filter { !onlyDeterministic || it.deterministic }
        .filter { (_, _, returnType) -> exprType.allows(returnType) }
        .let { oneOf(it) }
        .let { (name, params) ->
            val args = params.map { with(depth - 1, exprType = ExprType(listOf(it), true)).expr() }
            FunctionCall(name, args)
        }
}