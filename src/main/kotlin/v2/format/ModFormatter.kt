package v2.format

import v2.format.config.Config
import java.io.File
import java.io.IOException

class ModFormatter(private val modRootFolder: File, private val fileFormatter: FileFormatter) {
    companion object {
        private val goodExtensions = arrayOf("txt", "map")
    }

    init {
        if (!modRootFolder.isDirectory) {
            throw IOException("Mod folder '${modRootFolder.name}' is not a valid directory.")
        }
    }

    fun format(file: File) {
        file.walk()
            .filter(::isFormattable)
            .forEach(fileFormatter::formatFile)
    }

    fun isFormattable(file: File) = with(file) {
        isFile &&
                canWrite() &&
                extension in goodExtensions &&
                relativeTo(modRootFolder).path.asUnix() !in Config.excludeFiles &&
                canRead()
    }
}
