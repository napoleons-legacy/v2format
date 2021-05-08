package v2.format

import java.io.File

fun getResourceFile(resource: String): File =
    File(Main::class.java.getResource(resource)!!.toURI())
