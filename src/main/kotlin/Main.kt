package net.sebyte

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.default
import kotlinx.cli.optional
import net.sebyte.cfg.PRESET_CFG
import net.sebyte.cfg.RunConfig
import net.sebyte.cfg.SQLITE_v3_26_0
import net.sebyte.cli.Logger
import net.sebyte.run.*
import java.io.File


@OptIn(ExperimentalCli::class)
fun main(args: Array<String>) {
    val parser = ArgParser("test-db")

    val version by parser.option(ArgType.Boolean, "version", description = "Display version").default(false)
    val verbose by parser.option(ArgType.Boolean, "verbose", "v", "Display version").default(false)
    val preset by parser.option(ArgType.Boolean, "preset", description = "Use preset for testing").default(false)
    val config by parser.argument(ArgType.String, "config", "Path to config file").optional()
    parser.parse(args)

    Logger.verbose = verbose
    if (version) {
        Logger.info { "v0.1.0 by Sebastian Brunner" }
        return
    }

    if (preset && config != null) Logger.error { "Cannot use preset and config file simultaneously." }
    else if (!preset && config == null) Logger.error { "Have either use --preset or pass config file." }

    val configFile = config
    val cfg = if (configFile != null) RunConfig.from(configFile) else PRESET_CFG
    val trial = Trial.from(cfg)
    trial.run()
}