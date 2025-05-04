import net.sebyte.gen.SelectGenerator
import kotlin.random.Random

fun main() {
    val r = Random.Default
    val gen = SelectGenerator(r, mapOf(
        "t1" to listOf("c11", "c12"),
        "t2" to listOf("c21", "c22")
    ))
    for(i in 0..10) println(gen.select())
}