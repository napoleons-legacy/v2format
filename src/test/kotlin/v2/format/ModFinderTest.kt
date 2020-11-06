package v2.format

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileFilter
import java.io.IOException

internal class ModFinderTest {

    @Test
    fun `Test getModFolder with no mod file`() {
        val file = mockk<File>()
        every { file.listFiles(any<FileFilter>()) } returns emptyArray()

        val finder = ModFinder()
        assertThrows<IOException> { finder.getModFromDirectory(file) }
    }

    @Test
    fun `Test getModFolder with multiple mod files`() {
        val file = mockk<File>()
        every { file.listFiles(any<FileFilter>()) } returns arrayOf(File("fake1"), File("fake2"))

        val finder = ModFinder()
        assertThrows<IOException> { finder.getModFromDirectory(file) }
    }

    @Test
    fun `Test getModFolder with no path`() {
        val file = mockk<File>()
        val modFile = mockk<File>()

        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        every { file.listFiles(any<FileFilter>()) } returns arrayOf(modFile)
        every { modFile.name } returns "fake_name.mod"
        every { modFile.readLines() } returns listOf("name = Fake name", "user_dir = fake mod")

        val finder = ModFinder()
        assertThrows<IOException> { finder.getModFromDirectory(file) }
    }

    @Test
    fun `Test getModFolder with path`() {
        val file = mockk<File>()
        val modFile = mockk<File>()

        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        every { file.listFiles(any<FileFilter>()) } returns arrayOf(modFile)
        every { modFile.name } returns "fake_name.mod"
        every { modFile.readLines() } returns listOf(
            "name = Fake name",
            "path = mod/fake mod",
            "user_dir = fakemod"
        )

        val finder = ModFinder()
        val mod = finder.getModFromDirectory(file)
        assertFalse(mod.exists())
        assertEquals("fake mod", mod.name)
    }
}
