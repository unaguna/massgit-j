package jp.unaguna.massgit.testcommon.process

import jp.unaguna.massgit.ProcessExecutor
import jp.unaguna.massgit.testcommon.io.EmptyInputStream
import jp.unaguna.massgit.testcommon.io.EmptyOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

class DummyProcessExecutor(
    exitCodes: List<Int>,
    stdout: List<Path>? = null,
) : ProcessExecutor {
    val exitCodesStock = ArrayDeque(exitCodes)
    val stdoutStock = if (stdout != null) ArrayDeque(stdout) else null
    var executeCount = 0
        private set

    override fun execute(
        command: List<String>,
        workingDir: Path?
    ): Process {
        val nextExitCode = exitCodesStock.removeFirstOrNull() ?: 0
        val nextStdout = stdoutStock?.removeFirstOrNull()
        executeCount += 1

        return DummyProcess(nextExitCode, stdout = nextStdout)
    }

    class DummyProcess(
        private val exitCode: Int,
        private val stdout: Path? = null,
    ) : Process() {
        private val stdoutStream by lazy {
            stdout?.inputStream() ?: EmptyInputStream()
        }

        override fun getOutputStream(): OutputStream {
            return EmptyOutputStream()
        }

        override fun getInputStream(): InputStream {
            return stdoutStream
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
