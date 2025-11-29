package jp.unaguna.massgit.printmanager

import jp.unaguna.massgit.PrintFilter
import jp.unaguna.massgit.PrintManager
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.Charset
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.inputStream

class PrintManagerStoreAll(
    private val printFilter: PrintFilter,
    private val out: PrintStream = System.out,
) : PrintManager {
    val tmpFile = createTempFile(prefix = "massgit_")
    private val stdoutCharset = Charset.forName("utf8")

    override fun readAllLinesAndInstantOutput(stdout: InputStream) {
        val reader = InputStreamReader(stdout, Charset.forName("utf8"))

        tmpFile.bufferedWriter(stdoutCharset).use { writer ->
            reader.forEachLine { line ->
                writer.append(printFilter.mapLine(line))
                writer.newLine()
            }
        }
    }

    override fun postOutput(stdout: InputStream) {
        tmpFile.inputStream().copyTo(out)
    }

    override fun close() {
        tmpFile.deleteExisting()
    }
}
