package v2.format.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import v2.format.asUnix
import java.io.File

object Config {
    @Serializable
    private data class ConfigData(
        val paths: Map<String, FormatOptionsLayer>,
        val excludeFiles: List<String> = emptyList()
    )

    private val configTree = ConfigTree()
    lateinit var excludeFiles: List<String>

    fun loadConfig(file: File) {
        val json = Json(JsonConfiguration.Stable)

        if (file.exists()) {
            val configData = json.parse(ConfigData.serializer(), file.readText())
            excludeFiles = configData.excludeFiles.map(String::asUnix)
            buildTree(configData)
        } else {
            excludeFiles = emptyList()
        }
    }

    private fun buildTree(data: ConfigData) {
        data.paths.forEach { (path, opt) ->
            val levels = getLevels(path).toMutableList()
            configTree.insert(levels, opt)
        }
    }

    private fun getLevels(s: String) = s.split('/', '\\')
        .dropWhile(String::isBlank)

    operator fun get(key: String): FormatOptions {
        val levels = getLevels(key)
        return configTree[levels]
    }
}