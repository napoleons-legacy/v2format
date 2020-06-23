package v2.format

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNSimulator
import v2.format.antlr.ClausewitzLexer
import v2.format.antlr.ClausewitzParser
import v2.format.clausewitz.ClausewitzErrorListener
import v2.format.clausewitz.ClausewitzVisitor
import v2.format.config.Config
import v2.format.config.FormatOptions
import java.io.File
import java.nio.charset.Charset

class FileFormatter(private val modFolder: File) {
    private val charset = Charset.forName("cp1252")

    fun formatFile(file: File) {
        try {
            val relativeFile = file.relativeTo(modFolder)
            val options = Config[relativeFile.path]

            val formattedProgram = formatProgram(file, options)
            file.writeText(formattedProgram, charset)
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun formatProgram(file: File, options: FormatOptions): String {
        val lexer = ClausewitzLexer(CharStreams.fromStream(file.inputStream(), charset))
        lexer.attachListener(file)

        val tokenStream = CommonTokenStream(lexer)
        val parser = ClausewitzParser(tokenStream)
        parser.attachListener(file)

        val visitor = ClausewitzVisitor(tokenStream, options)

        val ctx = parser.program()
        return visitor.visitProgram(ctx)
    }

    private fun <T, Sim : ATNSimulator> Recognizer<T, Sim>.attachListener(file: File) {
        val listener = ClausewitzErrorListener(file)
        removeErrorListeners()
        addErrorListener(listener)
    }
}
