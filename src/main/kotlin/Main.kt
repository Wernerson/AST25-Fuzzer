package net.sebyte

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody

@Suppress("unused")
class Config(parser: ArgParser) {
    val justGenerate by parser.flagging(
        "--just-generate", "-g",
        help = "Generate the generated code"
    ).default(false)

    val numberOfQueries by parser.storing(
        "--number-of-queries", "-n",
        help = "Number of queries to generate",
    ) { toInt() }.default(100_000)

    val testPath by parser.storing(
        "--test-path",
        help = "Path to subject under test",
    ).default("/usr/bin/sqlite3-3.26.0")

    val oraclePath by parser.storing(
        "--oracle-path",
        help = "Path to test oracle",
    ).default("/usr/bin/sqlite3-3.39.4")

    val seed by parser.storing(
        "--seed", "-s",
        help = "Seed for randomness for reproducibility"
    ) { toInt() }.default(null)
}

fun main(args: Array<String>) = mainBody {
    val config = ArgParser(args).parseInto(::Config)
    if (config.justGenerate) TODO()
    else test(config)
}