package v2.format.config

import kotlinx.serialization.Serializable

@Serializable
data class FormatOptionsLayer(
    val tabWidth: Int? = null,
    val bracketSpacing: Boolean? = null,
    val assignmentSpacing: Boolean? = null,
    val singleLineBlock: Boolean? = null,
    val bracketWraparound: Int? = null
) {
    fun apply(options: FormatOptions) = FormatOptions(
        tabWidth ?: options.tabWidth,
        bracketSpacing ?: options.bracketSpacing,
        assignmentSpacing ?: options.assignmentSpacing,
        singleLineBlock ?: options.singleLineBlock,
        bracketWraparound ?: options.bracketWraparound
    )
}