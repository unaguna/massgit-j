package jp.unaguna.massgit.testcommon.io

import java.io.ByteArrayOutputStream
import java.io.PrintStream

fun buildStringByPrintStream(action: PrintStream.() -> Unit): String {
    val outputStream = ByteArrayOutputStream()
    PrintStream(outputStream).action()
    return outputStream.toString()
}
