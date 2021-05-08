package v2.format.config

import kotlinx.serialization.Serializable

@Serializable
data class FormatOptions(
    val useTab: Boolean = false,
    val tabWidth: Int = 4,
    val bracketSpacing: Boolean = true,
    val assignmentSpacing: Boolean = true,
    val singleLineBlock: Boolean = true,
    val bracketWraparound: Int = 10
) {
    private val tab = if (useTab) "\t" else " ".repeat(tabWidth)

    fun indent(indentation: Int) = tab.repeat(indentation)
}
