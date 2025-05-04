package net.sebyte.gen

import net.sebyte.ast.*
import kotlin.random.Random

data class Function(
    val name: String,
    val parameters: List<DataType>,
    val returnType: DataType,
    val deterministic: Boolean = true
)

class ExprGenerator(
    r: Random,
    private val input: DataSources
) : Generator(r) {
    fun literalValue(): LiteralValue = oneOf {
        add { LiteralValue.NumericLiteral(r.nextInt()) }
        add { LiteralValue.BlobLiteral(r.nextBytes(10)) }
        add {
            LiteralValue.StringLiteral(
                listOf(5..10, 'a'..'z').joinToString("", prefix = "\"", postfix = "\"")
            )
        }

        addAll(LiteralValue.Constants.entries.map { { it } })
    }

    fun tableColumn(): TableColumn = oneOf(
        input.flatMap { (table, columns) ->
            columns.map { TableColumn(table = table, column = it) }
        }
    )

    fun unaryExpr(): UnaryExpr = UnaryExpr(
        oneOf(UnaryExpr.Op.entries), expr()
    )

    fun binaryExpr(): BinaryExpr = BinaryExpr(
        expr(), oneOf(BinaryExpr.Op.entries), expr()
    )

    fun functionCall(): FunctionCall = oneOf(
        Function("abs", listOf(DataType.REAL), DataType.REAL)
    ).let { (name, params) ->
        val args = params.map { expr() } // todo type it
        FunctionCall(name, args)
    }

    fun expr(): Expr = oneOf {
        add(::literalValue)
        if (input.isNotEmpty()) add(::tableColumn)
        add(::unaryExpr)
        add(::binaryExpr)
        add(::functionCall)
        // todo tuple, cast collate
    }

    fun exprOrNull(nullPct: Double) =
        if (nextBoolean(nullPct)) null
        else expr()
}