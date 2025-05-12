package net.sebyte.cfg

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

enum class SQLiteConfig {
    v3_26_0, v3_39_4, v3_44_4
}

@Serializable
class RunConfig(
    val subject: String,
    val oracle: String? = null,
    val coverage: Boolean = false,
    val queries: Int = 1000,
    val mutations: Int? = null,
    val noTables: Int = 20,
    val noColumns: Int = 5,
    val testDb: String? = null,
    val archiveDir: String? = null,
    val generator: SQLiteConfig = SQLiteConfig.v3_26_0
) {
    companion object {
        fun from(filePath: String): RunConfig = Json.decodeFromString<RunConfig>(File(filePath).readText())
    }
}
