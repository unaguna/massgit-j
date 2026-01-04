package jp.unaguna.massgit.testcommon.process

import jp.unaguna.massgit.ProcessExecutor
import java.nio.file.Path

class PreErrorProcessExecutor : ProcessExecutor {
    var executeCount = 0
        private set
    private val commandHistory: MutableList<HistoryLine> = mutableListOf()

    override fun execute(
        command: List<String>,
        workingDir: Path?
    ): Process {
        executeCount += 1
        commandHistory.add(HistoryLine(command, workingDir))

        error("mock to error before start process")
    }

    fun getHistory(index: Int) = commandHistory[index]

    fun getHistories() = commandHistory.toList()

    data class HistoryLine(val command: List<String>, val workDir: Path?) {
        val dirname: String?
            get() {
                val indexOfC = command.indexOf("-C")
                return if (indexOfC == -1) null else command[indexOfC + 1]
            }
    }
}
