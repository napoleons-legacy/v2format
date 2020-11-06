package v2.format

import java.io.File
import java.io.IOException

class ModFinder {
    @Throws(IOException::class)
    fun getModFromDirectory(file: File): File {
        val modFiles = file.listFiles { pathName -> pathName.extension == "mod" }!!

        if (modFiles.isEmpty()) {
            throw IOException("No .mod file in directory found.")
        }

        if (modFiles.size > 1) {
            throw IOException("Multiple .mod files found, but only one should have been encountered.")
        }

        val firstModFile = modFiles.first()
        return file.resolve(getModFromModFile(firstModFile))
    }

    fun getModFromModFile(modFile: File): File {
        val line = modFile.readLines().firstOrNull {
            it.trimStart().startsWith("path")
        } ?: throw IOException("No entry named 'path' for mod file '${modFile.name}'")

        val modRootName = line.substringAfter('=').trim().removeSurrounding("\"").drop(4)
        return File(modRootName)
    }
}
