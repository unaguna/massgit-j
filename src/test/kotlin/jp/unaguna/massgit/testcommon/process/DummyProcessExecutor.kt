package jp.unaguna.massgit.testcommon.process

import jp.unaguna.massgit.ProcessExecutor
import jp.unaguna.massgit.testcommon.io.EmptyInputStream
import jp.unaguna.massgit.testcommon.io.EmptyOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path


class DummyProcessExecutor(
    exitCodes: List<Int>,
) : ProcessExecutor {
    val exitCodesStock = ArrayDeque(exitCodes)
    var executeCount = 0
        private set

    override fun execute(
        command: List<String>,
        workingDir: Path?
    ): Process {
        val nextExitCode = exitCodesStock.removeFirstOrNull() ?: 0
        executeCount += 1

        return DummyProcess(nextExitCode)
    }

    class DummyProcess(
        private val exitCode: Int,
    ) : Process() {
        override fun getOutputStream(): OutputStream {
            return EmptyOutputStream()
        }

        override fun getInputStream(): InputStream {
            return EmptyInputStream()
        }

        override fun getErrorStream(): InputStream {
            return EmptyInputStream()
        }

        override fun waitFor(): Int {
            return exitValue()
        }

        override fun exitValue(): Int {
            return exitCode
        }

        override fun destroy() {
            // do nothing
        }

    }
}
