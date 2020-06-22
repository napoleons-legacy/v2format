package v2.format

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
            .filter(::filterFile)
            .forEach(fileFormatter::formatFile)
    }

    private fun filterFile(file: File) = with(file) {
        isFile && extension in goodExtensions
    }
}