package org.weefic.cache

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.File
import kotlin.system.exitProcess

private const val DEFAULT_PORT = 80

private data class Arguments(val port: Int, val dir: File?)

fun main(args: Array<String>) {
    val arguments = parseArgumentsOrExit(args)
    App(arguments.port, arguments.dir)
}

private fun printUsageAndExit(options: Options): Nothing {
    HelpFormatter().printHelp(
        "java -jar WebCacheServer.jar",
        null,
        options,
        null,
        true
    )
    exitProcess(-1)
}


private fun parseArgumentsOrExit(args: Array<String>): Arguments {
    val options = Options().apply {
        addOption(Option.builder().longOpt("start").required().desc("start server").build())
        addOption(Option.builder("p").longOpt("port").hasArg().argName("port").desc("server port (default $DEFAULT_PORT)").build())
        addOption(Option.builder("d").longOpt("dir").hasArg().argName("file").desc("the directory where cache file to be store").build())
        addOption(Option.builder("h").longOpt("help").desc("show help info").build())
    }
    val parser = DefaultParser()
    try {
        val commandLine = parser.parse(options, args)
        if (commandLine.hasOption("help")) {
            printUsageAndExit(options)
        } else if (commandLine.hasOption("start")) {
            var port = DEFAULT_PORT
            if (commandLine.hasOption("port")) {
                port = commandLine.getOptionValue("port").toInt() // or throw
            }
            val dir: String? = commandLine.getOptionValue("dir")
            val dirFile = dir?.let { File(it) }
            return Arguments(port, dirFile)
        } else {
            printUsageAndExit(options)
        }
    } catch (e: Exception) {
        printUsageAndExit(options)
    }
}

