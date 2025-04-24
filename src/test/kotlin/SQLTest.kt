import net.sebyte.Select
import net.sebyte.createDataSources
import net.sebyte.createDatabase
import kotlin.random.Random

fun main() {
    val rand = Random.Default
    val dataSources = createDataSources(rand)
    println(createDatabase(rand, dataSources))
    for(i in 1..100_000) {
        println(Select.rand(rand, dataSources).first)
    }
}