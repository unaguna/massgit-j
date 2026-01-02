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

fun <R> trapStdoutAndResult(action: () -> R): Pair<String, R> {
    return TrapStdout().use { trapInstance ->
        val result = action()
        val stdout = trapInstance.getTrappedString()
        Pair(stdout, result)
    }
}

fun trapStderr(action: () -> Unit): String {
    return TrapStderr().use { trapInstance ->
        action()
        trapInstance.getTrappedString()
    }
}

fun trapStdoutStderr(action: () -> Unit): Trapped {
    return TrapStdout().use { trapStdoutInstance ->
        TrapStderr().use { trapStderrInstance ->
            action()
            Trapped(out = trapStdoutInstance.getTrappedString(), err = trapStderrInstance.getTrappedString())
        }
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

private class TrapStderr : Closeable {
    private val defaultStderr = System.err
    private val newOutputStream = ByteArrayOutputStream()
    private val newStderr = PrintStream(newOutputStream)

    init {
        System.setErr(newStderr)
    }

    fun getTrappedString(): String {
        return newOutputStream.toString()
    }

    override fun close() {
        System.setErr(defaultStderr)
        newStderr.close()
    }
}

data class Trapped(
    val out: String,
    val err: String,
)
