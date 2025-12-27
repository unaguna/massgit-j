package jp.unaguna.massgit

import kotlin.concurrent.thread

class ProcessController(
    private val process: Process,
    private val printManager: PrintManager,
    private val printErrorManager: PrintManager,
) {
    fun readOutput() {
        val stdoutReadThread = thread {
            printManager.readAllLinesAndInstantOutput(process.inputStream)
        }
        val stderrReadThread = thread {
            printErrorManager.readAllLinesAndInstantOutput(process.errorStream)
        }
        process.waitFor()
        stdoutReadThread.join()
        stderrReadThread.join()

        // TODO: ロックする
        printManager.postOutput(process.inputStream)
        printErrorManager.postOutput(process.errorStream)
    }
}
