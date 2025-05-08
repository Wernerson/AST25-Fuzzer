package net.sebyte.cfg

import net.sebyte.ast.BinaryExpr
import net.sebyte.ast.BinaryExpr.Op.*
import net.sebyte.ast.JoinClause.JoinOperator
import net.sebyte.ast.JoinClause.JoinOperator.*
import kotlin.random.Random

class GeneratorConfig(
    seed: Long? = null,
    supportedFunctions: List<String>,
    val supportedJoinOperators: List<JoinOperator> = JoinOperator.entries,
    unsupportedOperators: List<BinaryExpr.Op> = emptyList(),
    val orderNulls: Boolean = true,
    val maxExprDepth: Int = 5,
    val literalIntRange: IntRange = -10..10,
    val literalBlobSize: Int = 10,
    val literalTextSizeRange: IntRange = 5..10,
    val maxFromDepth: Int = 3,
    val maxSelectDepth: Int = 3,
    val wherePct: Double = 0.2,
    val groupByPct: Double = 0.8,
    val havingPct: Double = 0.2,
    val orderByPct: Double = 0.8,
    val limitPct: Double = 0.8,
    val offsetPct: Double = 0.5
) {
    val r = seed?.let { Random(it) } ?: Random.Default
    val supportedBinaryOps = BinaryExpr.Op.entries.filter { it !in unsupportedOperators }
    private val supportedFunctions = supportedFunctions.associateWith { true }

    fun supports(functionName: String) = supportedFunctions.getOrDefault(functionName, false)
}

val SQLITE_v3_44_4 = GeneratorConfig(
    supportedFunctions = listOf(
        "abs",
        "changes", "char", "coalesce", "concat",
        "glob",
        "hex", "ifnull", "instr",
        "last_insert_rowid", "length", "like", "likely", "lower", "ltrim",
        "max", "min",
        "nullif",
        "octet_length",
        "quote",
        "random", "randomblob", "replace", "round", "rtrim",
        "sign", "sqlite_source_id", "sqlite_version", "substr", "substring",
        "total_changes", "trim", "typeof",
        "unicode", "unlikely", "upper",
        "zeroblob"
    )
)

val SQLITE_v3_39_4 = GeneratorConfig(
    supportedFunctions = listOf(
        "abs",
        "changes", "char", "coalesce",
        "glob",
        "hex", "ifnull", "instr",
        "last_insert_rowid", "length", "like", "likely", "lower", "ltrim",
        "max", "min",
        "nullif",
        "quote",
        "random", "randomblob", "replace", "round", "rtrim",
        "sign", "sqlite_source_id", "sqlite_version", "substr", "substring",
        "total_changes", "trim", "typeof",
        "unicode", "unlikely", "upper",
        "zeroblob"
    )
)

val SQLITE_v3_26_0 = GeneratorConfig(
    supportedFunctions = listOf(
        "abs",
        "changes", "char", "coalesce",
        "glob",
        "hex", "ifnull", "instr",
        "last_insert_rowid", "length", "like", "likely", "lower", "ltrim",
        "max", "min",
        "nullif",
        "quote",
        "random", "randomblob", "replace", "round", "rtrim",
        "sqlite_source_id", "sqlite_version", "substr",
        "total_changes", "trim", "typeof",
        "unicode", "unlikely", "upper",
        "zeroblob"
    ), supportedJoinOperators = listOf(
        LEFT, INNER, CROSS,
        NATURAL_LEFT, NATURAL_INNER
    ), unsupportedOperators = listOf(
        EXTRACT, EXTRACT2, IS_DISTINCT_FROM, IS_NOT_DISTINCT_FROM
    ), orderNulls = false
)