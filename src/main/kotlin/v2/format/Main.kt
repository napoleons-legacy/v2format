package v2.format

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import v2.format.config.Config
import java.io.File

class Main : CliktCommand(help = "A program formatter for Victoria 2 mods.") {
    private val folder by option(help = "Specifies the mod directory, otherwise it attempts to find the mod in the current directory")
        .file(
            mustExist = true,
            canBeFile = false,
            canBeDir = true,
            mustBeReadable = true,
            mustBeWritable = true
        )

    private val file by option(help = "Specifies the file to format relative to the mod directory")
        .file(
            mustExist = true,
            canBeFile = true,
            canBeDir = false,
            mustBeReadable = true,
            mustBeWritable = true
        )

    private val config by option(help = "Specifies the configuration file, otherwise it uses the file 'v2format.config.json'")
        .file(
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
        val finder = ModFinder()
        val modFolder = folder ?: finder.getModFolder(File("."))
        val modConfig = config ?: modFolder.resolveSibling("v2format.config.json")

        try {
            Config.loadConfig(modConfig)
        } catch (e: Exception) {
            val cleanMessage = e.message.orEmpty()
                .replace(" You can enable 'JsonConfiguration.ignoreUnknownKeys' property to ignore unknown keys.", "")
            throw RuntimeException("Config file ${modConfig.name} is incorrect. \n$cleanMessage")
        }

        val fileFormatter = FileFormatter(modFolder)
        val modFormatter = ModFormatter(modFolder, fileFormatter)

        if (file != null) {
            if (modFormatter.isFormattable(file!!)) {
                fileFormatter.formatFile(file!!)
            } else {
                throw RuntimeException("File ${file!!.name} cannot be formatted due to its extension or exclusion configuration.")
            }
        } else {
            modFormatter.format()
        }
    }
}

fun main(args: Array<String>) = Main().main(args)
