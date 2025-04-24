package net.sebyte

import kotlin.random.Random
import kotlin.random.nextInt

enum class DataType {
    INTEGER, REAL, TEXT, BLOB;

    @OptIn(ExperimentalStdlibApi::class)
    fun rand(rand: Random): String = when(this) {
        INTEGER -> rand.nextInt(-10..10).toString()
        REAL -> rand.nextDouble(-1.0, 1.0).toString()
        TEXT -> "'${(1..rand.nextInt(1..10)).map { ('a'..'z').random(rand) }.joinToString(separator = "")}'"
        BLOB -> "X'${rand.nextBytes(rand.nextInt(1..10)).toHexString()}'"
    }
}

data class DataColumn(
    val name: String,
    val type: DataType,
    val nullable: Boolean
) {
    override fun toString() = "$name $type ${if (nullable) "NULL" else "NOT NULL"}"

    fun rand(rand: Random): String {
        if (nullable && rand.nextBoolean()) return "NULL"
        return type.rand(rand)
    }
}

data class DataSource(
    val name: String,
    val columns: List<DataColumn>
)

fun createDataSources(rand: Random, noTables: IntRange = 5..10) = List(rand.nextInt(noTables)) { ti ->
    DataSource(
        name = "t$ti",
        columns = List(rand.nextInt(1..10)) {
            DataColumn("t${ti}_c$it", DataType.entries.random(rand), rand.nextBoolean())
        }
    )
}