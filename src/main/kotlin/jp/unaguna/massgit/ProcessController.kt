package jp.unaguna.massgit

class ProcessController(
    private val process: Process,
    private val printManager: PrintManager,
) {
    fun readOutput() {
        printManager.readAllLinesAndInstantOutput(process.inputStream)
        process.waitFor()

        // TODO: ロックする
        printManager.postOutput(process.inputStream)
    }
}
