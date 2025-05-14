package net.sebyte

import kotlinx.cli.*
import net.sebyte.cfg.RunConfig
import net.sebyte.cli.Logger
import net.sebyte.run.Trial


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")

    val version by parser.option(ArgType.Boolean, "version", description = "Display version").default(false)
    val verbose by parser.option(ArgType.Boolean, "verbose", "v", "Display version").default(false)
    val config by parser.argument(ArgType.String, "config", "Path to config file").optional()
    parser.parse(args)

    Logger.verbose = verbose
    if (version) {
        Logger.info { "v0.1.0 by Sebastian Brunner" }
        return
    }

    val cfg = config?.let { RunConfig.from(it) } ?: RunConfig()
    val trial = Trial.from(cfg)
    trial.run()
}