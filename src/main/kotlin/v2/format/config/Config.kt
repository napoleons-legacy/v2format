package v2.format.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import v2.format.asUnix
import java.io.File

const val CONFIG_DEFAULT_NAME = "v2format.config.json"

object Config {
    @Serializable
    private data class ConfigData(
        val paths: Map<String, FormatOptionsLayer>,
        val excludeFiles: List<String> = emptyList()
    )

    private var configTree = ConfigTree()
    lateinit var excludeFiles: List<String>

    fun loadConfig(file: File) {
        val json = Json {
            ignoreUnknownKeys = true
        }

        if (file.exists()) {
            val configData = json.decodeFromString(ConfigData.serializer(), file.readText())
            excludeFiles = configData.excludeFiles.map(String::asUnix)
            buildTree(configData)
        } else {
            excludeFiles = emptyList()
        }
    }

    fun clearConfig() {
        configTree = ConfigTree()
    }

    private fun buildTree(data: ConfigData) {
        data.paths.forEach { (path, opt) ->
            val levels = getLevels(path).toMutableList()
            configTree.insert(levels, opt)
        }
    }

    private fun getLevels(s: String) = s.split('/', '\\')
        .filterNot(String::isBlank)

    operator fun get(key: String): FormatOptions {
        val levels = getLevels(key)
        return configTree[levels]
    }
}
