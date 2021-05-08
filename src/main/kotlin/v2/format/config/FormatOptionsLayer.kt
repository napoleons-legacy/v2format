package v2.format.config

import kotlinx.serialization.Serializable

@Serializable
data class FormatOptionsLayer(
    val useTab: Boolean? = null,
    val tabWidth: Int? = null,
    val bracketSpacing: Boolean? = null,
    val assignmentSpacing: Boolean? = null,
    val singleLineBlock: Boolean? = null,
    val bracketWraparound: Int? = null
) {
    fun apply(options: FormatOptions) = FormatOptions(
        useTab ?: options.useTab,
        tabWidth ?: options.tabWidth,
        bracketSpacing ?: options.bracketSpacing,
        assignmentSpacing ?: options.assignmentSpacing,
        singleLineBlock ?: options.singleLineBlock,
        bracketWraparound ?: options.bracketWraparound
    )
}
