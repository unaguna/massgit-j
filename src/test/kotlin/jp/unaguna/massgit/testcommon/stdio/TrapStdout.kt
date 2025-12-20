package jp.unaguna.massgit.testcommon.stdio

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.PrintStream

fun trapStdout(action: () -> Unit): String {
    return TrapStdout().use { trapInstance ->
        action()
        trapInstance.getTrappedString()
    }
}

private class TrapStdout : Closeable {
    private val defaultStdout = System.out
    private val newOutputStream = ByteArrayOutputStream()
    private val newStdout = PrintStream(newOutputStream)

    init {
        System.setOut(newStdout)
    }

    fun getTrappedString(): String {
        return newOutputStream.toString()
    }

    override fun close() {
        System.setOut(defaultStdout)
        newStdout.close()
    }
}
