package v2.format

import v2.format.config.Config
import java.io.File
import java.io.IOException

class ModFormatter(private val modFolder: File, private val fileFormatter: FileFormatter) {
    private val goodExtensions = arrayOf("txt", "map")

    init {
        if (!modFolder.isDirectory) {
            throw IOException("Mod folder '${modFolder.name}' is not a valid directory.")
        }
    }

    fun format() {
        modFolder.walk()
            .filter(::isFormattable)
            .forEach(fileFormatter::formatFile)
    }

    fun isFormattable(file: File) = with(file) {
        isFile && extension in goodExtensions && relativeTo(modFolder).path.asUnix() !in Config.excludeFiles
    }
}
