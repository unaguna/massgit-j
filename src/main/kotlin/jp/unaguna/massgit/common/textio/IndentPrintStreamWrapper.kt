package jp.unaguna.massgit.common.textio

import java.io.PrintStream

class IndentPrintStreamWrapper(private val printStream: PrintStream, private val windowWidth: Int = 120) {
    private var indent = 0
    private var offsetInLine = 0

    fun addIndent(value: Int) {
        this.indent += value
    }

    fun println() {
        newline()
    }

    fun println(text: String) {
        print(text)
        newline()
    }

    fun print(text: String) {
        text.split(" ").forEachIndexed { index, word ->
            if (offsetInLine > 0 && offsetInLine + 1 + word.length > windowWidth) {
                newline()
                applyIndent()
            } else if (offsetInLine > 0 && index > 0) {
                printStream.print(" ")
                offsetInLine += 1
            } else if (offsetInLine == 0) {
                applyIndent()
            }

            printStream.print(word)
            offsetInLine += word.length
        }
    }

    fun applyIndent() {
        check(offsetInLine == 0) { "offsetInLine must be > 0" }

        repeat(indent) {
            printStream.print(" ")
        }
        offsetInLine += indent
    }

    fun newline() {
        printStream.println()
        offsetInLine = 0
    }
}
