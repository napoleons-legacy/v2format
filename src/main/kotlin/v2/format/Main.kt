package v2.format

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import v2.format.config.CONFIG_DEFAULT_NAME
import v2.format.config.Config
import java.io.File

class FormatOptions : OptionGroup() {
    val modFile by option("--mod", help = "Specifies the mod folder to format").file(
        mustExist = true,
        canBeFile = false,
        canBeDir = true,
        mustBeReadable = true
    ).required()

    val file by option("--file", help = "Specifies the file or folder to format").file(
        mustExist = true,
        mustBeReadable = true,
        mustBeWritable = true
    )
}

class Main : CliktCommand(help = "A program formatter for Victoria 2 mods.") {
    private val formatOptions by FormatOptions().cooccurring()

    private val configOption by option(
        "--config",
        help = "Specifies the configuration file, otherwise it uses the file '$CONFIG_DEFAULT_NAME'"
    ).file(
        mustExist = true,
        canBeFile = true,
        canBeDir = false,
        mustBeReadable = true
    )

    init {
        context {
            helpFormatter = CliktHelpFormatter(showDefaultValues = true)
        }
    }

    override fun run() {
        try {
            formatMod()
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    private fun formatMod() {
        val modFinder = ModFinder()

        val formatModRoot = formatOptions?.modFile ?: modFinder.getModFromDirectory(File("."))
        val formatTarget = formatOptions?.file ?: formatModRoot
        val formatConfig = resolveConfig(formatModRoot)

        try {
            Config.loadConfig(formatConfig)
        } catch (e: Exception) {
            throw RuntimeException("Config file '${formatConfig.name}' is invalid. \n${e.message}")
        }

        val fileFormatter = FileFormatter(formatModRoot)
        val modFormatter = ModFormatter(formatModRoot, fileFormatter)

        if (formatTarget.isDirectory && formatTarget.canRead() || modFormatter.isFormattable(formatTarget)) {
            modFormatter.format(formatTarget)
        } else {
            throw RuntimeException(
                "Format target '${formatTarget.name}' cannot be formatted due to its " +
                        "extension, exclusion configuration, or read/write property."
            )
        }
    }

    private fun resolveConfig(formatModRoot: File): File {
        if (configOption != null) {
            return configOption!!
        }

        val targetConfig = formatModRoot.resolveSibling(CONFIG_DEFAULT_NAME)
        if (targetConfig.exists()) {
            return targetConfig
        }

        return File(CONFIG_DEFAULT_NAME)
    }
}

fun main(args: Array<String>) = Main().main(args)