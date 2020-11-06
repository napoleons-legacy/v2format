package v2.format.clausewitz

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.runtime.tree.TerminalNode
import v2.format.antlr.ClausewitzLexer
import v2.format.antlr.ClausewitzParser
import v2.format.antlr.ClausewitzParserBaseVisitor
import v2.format.config.FormatOptions

class ClausewitzVisitor(
    private val tokenStream: TokenStream,
    private val options: FormatOptions
) : ClausewitzParserBaseVisitor<CharSequence>() {
    private var indentation = 0

    override fun visitProgram(ctx: ClausewitzParser.ProgramContext) = buildString {
        appendCosmetic(-1, false)

        for (child in ctx.expr()) {
            append(visit(child))
        }

        dropLastNewlines()
        appendln()
    }

    override fun visitAssignExpr(ctx: ClausewitzParser.AssignExprContext) =
        StringBuilder(visitAssignType(ctx.assignType()))
            .appendAround(options.assignmentSpacing, "=")
            .append(visitAssignValue(ctx.assignValue()))
            .appendCosmetic(ctx.stop.tokenIndex, true)

    override fun visitBraceExpr(ctx: ClausewitzParser.BraceExprContext): StringBuilder {
        val sb = StringBuilder("{")

        indentBlock {
            sb.appendCosmetic(ctx.L_BRACE().symbol.tokenIndex, false)
        }

        val values = ctx.braceValue()
        if (values.isEmpty()) {
            appendEmptyBraceValue(sb)
        } else {
            val isExprTypeBlock = values.all { it.exprType() != null }
            val block = if (isExprTypeBlock) {
                ::appendExprTypeBlock
            } else {
                ::appendExprBlock
            }

            appendBraceValueBlock(sb, values, block)
        }

        return sb.append('}')
    }

    private inline fun appendBraceValueBlock(
        sb: StringBuilder,
        values: List<ClausewitzParser.BraceValueContext>,
        block: (StringBuilder, List<ClausewitzParser.BraceValueContext>) -> Unit
    ) = if (values.size == 1) {
        appendSingleBraceValue(sb, values.first())
    } else {
        block(sb, values)
    }

    private fun appendExprTypeBlock(
        sb: StringBuilder,
        values: List<ClausewitzParser.BraceValueContext>
    ) {
        indentBlock {
            var idx = 0
            values.forEach {
                if (idx % options.bracketWraparound == 0) {
                    sb.cleanIndent()
                } else {
                    sb.append(' ')
                }

                sb.append(visit(it)).appendCosmetic(it.start.tokenIndex, false)

                idx = if (sb.endsWith("\n")) 0 else idx + 1
            }
        }

        sb.dropLastNewlines().indentln()
    }

    private fun appendExprBlock(sb: StringBuilder, values: List<ClausewitzParser.BraceValueContext>) {
        indentBlock {
            values.forEach {
                sb.cleanIndent().append(visit(it))
            }
        }

        sb.dropLastNewlines().indentln()
    }

    private fun appendEmptyBraceValue(sb: StringBuilder) {
        if (!options.singleLineBlock && sb.endsWith('{')) {
            sb.indentln()
        } else if (sb.endsWith('\n')) {
            sb.indent()
        }
    }

    private fun appendSingleBraceValue(sb: StringBuilder, first: ClausewitzParser.BraceValueContext) {
        val isAssignBraceExpr = isAssignBraceExpr(first)
        if (!options.singleLineBlock || isAssignBraceExpr || anyCommentsAfter(first)) {
            indentBlock {
                sb.cleanIndent().append(visit(first))

                if (first.exprType() != null) {
                    val cosmetic = getCosmetic(first.stop.tokenIndex, false).trim()
                    if (cosmetic.isNotEmpty()) {
                        sb.append(' ').append(cosmetic)
                    }
                }
            }

            sb.dropLastNewlines().indentln()
        } else {
            val firstValue = with(visit(first)) {
                if (this is StringBuilder) {
                    dropLastNewlines()
                } else {
                    dropLastWhile { it == '\n' }
                }
            }

            sb.appendAround(options.bracketSpacing, firstValue)
        }
    }

    override fun visitTerminal(node: TerminalNode) = node.text

    override fun defaultResult() = ""

    private fun anyCommentsAfter(first: ParserRuleContext): Boolean {
        for (idx in first.stop.tokenIndex + 1 until tokenStream.size()) {
            val chan = tokenStream[idx].channel
            if (chan == ClausewitzLexer.COMMENTS_CHANNEL) {
                return true
            } else if (chan == ClausewitzLexer.DEFAULT_TOKEN_CHANNEL) {
                return false
            }
        }

        return false
    }

    private fun isAssignBraceExpr(first: ClausewitzParser.BraceValueContext) =
        first.expr()?.assignExpr()?.assignValue()?.braceExpr() != null

    private inline fun indentBlock(block: () -> Unit) {
        indentation++
        block()
        indentation--
    }

    private fun getCosmetic(start: Int, allowWhitespace: Boolean): CharSequence {
        val sb = StringBuilder()

        var prev: Token? = if (start < 0) null else tokenStream[start]
        var idx = start + 1
        while (idx < tokenStream.size()) {
            val next = tokenStream[idx]

            when (next.channel) {
                ClausewitzLexer.COMMENTS_CHANNEL -> {
                    if (prev != null) {
                        if (prev.channel == ClausewitzLexer.NEWLINE_CHANNEL) {
                            sb.indent()
                        } else {
                            sb.append(' ')
                        }
                    }

                    sb.append(next.text.trimEnd())
                }
                ClausewitzLexer.NEWLINE_CHANNEL -> {
                    sb.append(next.text.replace("\r", ""))
                }
                else -> break
            }

            prev = next
            idx++
        }

        if (!allowWhitespace && sb.isBlank()) {
            return ""
        }

        return sb
    }

    private fun StringBuilder.dropLastNewlines(): StringBuilder {
        val lastNewline = indexOfLast {
            it != '\n'
        } + 1

        setLength(lastNewline)
        return this
    }

    private fun StringBuilder.appendAround(flag: Boolean, s: CharSequence): StringBuilder = if (flag) {
        append(' ').append(s).append(' ')
    } else {
        append(s)
    }

    private fun StringBuilder.appendCosmetic(start: Int, allowWhitespace: Boolean): StringBuilder =
        append(getCosmetic(start, allowWhitespace))

    private fun StringBuilder.appendln(): StringBuilder = append('\n')
    private fun StringBuilder.indent(): StringBuilder = append(options.indent(indentation))
    private fun StringBuilder.indentln(): StringBuilder = appendln().indent()
    private fun StringBuilder.cleanIndent(): StringBuilder = if (endsWith("\n")) {
        indent()
    } else {
        indentln()
    }
}
