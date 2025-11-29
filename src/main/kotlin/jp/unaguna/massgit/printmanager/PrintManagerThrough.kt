package jp.unaguna.massgit.printmanager

import jp.unaguna.massgit.PrintFilter
import jp.unaguna.massgit.PrintManager
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset

class PrintManagerThrough(
    private val printFilter: PrintFilter,
    private val out: PrintStream = System.out,
) : PrintManager {
    override fun readAllLinesAndInstantOutput(stdout: InputStream) {
        val reader = InputStreamReader(stdout, Charset.forName("utf8"))

        reader.forEachLine { line ->
            out.println(printFilter.mapLine(line))
        }
    }

    override fun postOutput(stdout: InputStream) {
        // do nothing
    }

    override fun close() {
        // do nothing
    }
}
