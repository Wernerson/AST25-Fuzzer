package net.sebyte.cfg

class RunConfig(
    val subject: String?,
    val oracle: String?,
    val coverage: Boolean,
    val queries: Int,
    val mutations: Int?,
    val noTables: Int,
    val noColumns: Int,
    val generator: GeneratorConfig
)