package v2.format

import v2.format.config.Config
import v2.format.config.ConfigTree
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun getResourceFile(resource: String): File =
    File(File::class.java.getResource(resource).toURI())

fun resetConfig() {
    val tree = Config.javaClass.getDeclaredField("configTree")
    tree.isAccessible = true

    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.isAccessible = true
    modifiersField.setInt(tree, tree.modifiers and Modifier.FINAL.inv())
    tree.set(tree.javaClass.kotlin.objectInstance, ConfigTree())
}