package net.sebyte

import kotlin.random.Random

private const val NUM_QUERIES = 5

fun main() {
    val rand = Random.Default
    val dataSources = createDataSources(rand)
    println(createDatabase(rand, dataSources))
    for (i in 0..NUM_QUERIES) {
        val (query, _) = Select.rand(rand, dataSources)
        println(query)
    }
}