package v2.format

import java.io.File
import java.io.IOException

class ModFinder {
    @Throws(IOException::class)
    fun getModFolder(file: File): File {
        val modFiles = file.listFiles { pathName -> pathName.extension == "mod" }!!

        if (modFiles.isEmpty()) {
            throw IOException("No .mod file in directory found.")
        }

        if (modFiles.size > 1) {
            throw IOException("Multiple .mod files found, but only one should have been encountered.")
        }

        val firstModFile = modFiles.first()
        return file.resolve(getModDirectory(firstModFile))
    }

    private fun getModDirectory(modFile: File): File {
        val line = modFile.readLines().firstOrNull {
            it.trimStart().startsWith("user_dir")
        } ?: throw IOException("No entry named 'user_dir' for mod file '${modFile.name}'")

        val modFolderName = line.substringAfter('=').trim().removeSurrounding("\"")
        return File(modFolderName)
    }
}
