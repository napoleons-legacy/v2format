package v2.format

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import v2.format.config.Config
import java.io.File
import java.io.IOException

@ExtendWith(MockKExtension::class)
internal class ModFormatterTest {
    @MockK
    lateinit var fileFormatter: FileFormatter

    @Test
    fun `Test nonexistent mod folder`() {
        val file = mockk<File>()
        every { file.isDirectory } returns false
        every { file.name } returns "fake_name"

        assertThrows<IOException> {
            ModFormatter(file, fileFormatter)
        }
    }

    @Test
    fun `Test empty mod folder`() {
        val file = mockk<File>()
        every { file.isDirectory } returns true

        mockkStatic("kotlin.io.FilesKt__FileTreeWalkKt")

        val fileTreeWalk = mockk<FileTreeWalk>()
        every { file.walk() } returns fileTreeWalk
        every {
            fileTreeWalk.iterator()
        } returns emptyList<File>().iterator()

        val modFormatter = ModFormatter(file, fileFormatter)
        modFormatter.format(file)

        verify { fileFormatter wasNot Called }
    }

    @Test
    fun `Test unusable mod folder`() {
        Config.excludeFiles = emptyList()

        val file = mockk<File>()
        every { file.isDirectory } returns true

        mockkStatic("kotlin.io.FilesKt__FileTreeWalkKt")

        val modFiles = listOf(
            mockFile("junk.lua", true),
            mockFile("useless.rb", true),
            mockFile("v2.kt", true),
            mockFile("format.cpp", false),
            mockFile("folder.txt/", false, canRead = false),
            mockFile("unreadable.txt", true, canRead = false, canWrite = true),
            mockFile("unwriteable.txt", true, canRead = true, canWrite = false),
            mockFile("untouchable.txt", true, canRead = false, canWrite = false)
        )

        val fileTreeWalk = mockk<FileTreeWalk>()
        every { file.walk() } returns fileTreeWalk
        every {
            fileTreeWalk.iterator()
        } returns modFiles.iterator()

        every {
            file.path
        } returns "mock/path"

        val modFormatter = ModFormatter(file, fileFormatter)
        modFormatter.format(file)

        verify { fileFormatter wasNot Called }
    }

    @Test
    fun `Test good mod folder`() {
        Config.excludeFiles = emptyList()

        val file = mockk<File>()
        every { file.isDirectory } returns true

        mockkStatic("kotlin.io.FilesKt__FileTreeWalkKt")

        val modFiles = listOf(
            mockFile("valid.txt", true),
            mockFile("clean.txt", true),
            mockFile("directory/", false),
            mockFile("unreadable/", false, canRead = false),
            mockFile("nice.map", true)
        )

        val fileTreeWalk = mockk<FileTreeWalk>()
        every { file.walk() } returns fileTreeWalk
        every {
            fileTreeWalk.iterator()
        } returns modFiles.iterator()

        every {
            fileFormatter.formatFile(any())
        } just Runs

        every {
            file.path
        } returns "mock/path"

        val modFormatter = ModFormatter(file, fileFormatter)
        modFormatter.format(file)

        verify(exactly = 3) { fileFormatter.formatFile(any()) }
    }

    private fun mockFile(name: String, isFile: Boolean, canRead: Boolean = true, canWrite: Boolean = true): File {
        val mock = mockk<File>()
        every { mock.name } returns name
        every { mock.isFile } returns isFile
        every { mock.isDirectory } returns !isFile
        every { mock.path } returns name
        every { mock.canRead() } returns canRead
        every { mock.canWrite() } returns canWrite

        return mock
    }
}
