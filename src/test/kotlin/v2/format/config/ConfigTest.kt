package v2.format.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import v2.format.getResourceFile
import v2.format.resetConfig

internal class ConfigTest {

    @Test
    fun `Test loadConfig`() {

        testConfig("/config/blank.config.json") {
            val default = FormatOptions()

            assertKey("/", default)
            assertKey("/map/default.map", default)
            assertKey("/news", default)

            assertEquals(emptyList<String>(), Config.excludeFiles)
        }

        testConfig("/config/default.config.json") {
            val default = FormatOptions(tabWidth = 2)

            assertKey("/", default)
            assertKey("/map/default.map", default.copy(singleLineBlock = false, bracketWraparound = 25))
            assertKey("/news", default.copy(bracketWraparound = 1))

            assertEquals(listOf("map/positions.txt"), Config.excludeFiles)
        }

        testConfig("/config/complex.config.json") {
            val default = FormatOptions(tabWidth = 2)

            assertKey("/", default)
            assertKey("/map", default.copy(bracketSpacing = false))
            assertKey("map", default.copy(bracketSpacing = false))
            assertKey("map/terrain", default.copy(bracketSpacing = false, assignmentSpacing = false))
            assertKey(
                "map/terrain/rivers",
                default.copy(tabWidth = 3, assignmentSpacing = true, singleLineBlock = false, bracketSpacing = true)
            )
            assertKey(
                "map/default.map",
                default.copy(bracketSpacing = false, singleLineBlock = false, bracketWraparound = 25)
            )
            assertKey("/news", default.copy(bracketWraparound = 1))

            assertEquals(
                listOf(
                    "/map/positions.txt", "map/region.txt", "event/dummy.txt", "a/b/c/d/e/f.txt"
                ), Config.excludeFiles
            )
        }
    }

    private fun assertKey(key: String, options: FormatOptions) = assertEquals(options, Config[key])

    private inline fun testConfig(resource: String, block: () -> Unit) {
        Config.loadConfig(getResourceFile(resource))
        block()

        resetConfig()
    }
}