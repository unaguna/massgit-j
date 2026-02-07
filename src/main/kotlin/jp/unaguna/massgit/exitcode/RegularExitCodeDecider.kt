package jp.unaguna.massgit.exitcode

import jp.unaguna.massgit.ExitCodeDecider
import jp.unaguna.massgit.GitProcessResult
import jp.unaguna.massgit.common.collection.groupByType

class RegularExitCodeDecider<An> : ExitCodeDecider<An> {
    @Suppress("MagicNumber")
    override fun decideExitCode(results: List<GitProcessResult<An>>): Int {
        val (processes, throwable) = results.map { it.process }.groupByType()
        val summarizedExitCode = processes.maxOfOrNull { p -> p.exitValue() }

        return when {
            throwable.isNotEmpty() -> 127
            summarizedExitCode != null -> summarizedExitCode
            else -> error("no exit code found")
        }
    }
}
