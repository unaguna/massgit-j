package jp.unaguna.massgit

import kotlin.concurrent.thread

class ProcessController(
    private val process: Process,
    private val printManager: PrintManager,
    private val printErrorManager: PrintManager,
) {
    fun readOutput() {
        thread {
            printManager.readAllLinesAndInstantOutput(process.inputStream)
        }
        thread {
            printErrorManager.readAllLinesAndInstantOutput(process.errorStream)
        }
        process.waitFor()

        // TODO: ロックする
        printManager.postOutput(process.inputStream)
        printErrorManager.postOutput(process.errorStream)
    }
}
