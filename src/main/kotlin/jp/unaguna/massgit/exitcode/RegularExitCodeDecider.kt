package jp.unaguna.massgit.exitcode

import jp.unaguna.massgit.ExitCodeDecider
import jp.unaguna.massgit.common.collection.Either
import jp.unaguna.massgit.common.collection.groupByType

class RegularExitCodeDecider : ExitCodeDecider {
    @Suppress("MagicNumber")
    override fun decideExitCode(results: List<Either<Process, Throwable>>): Int {
        val (processes, throwable) = results.groupByType()
        val summarizedExitCode = processes.maxOfOrNull { p -> p.exitValue() }

        return when {
            throwable.isNotEmpty() -> 127
            summarizedExitCode != null -> summarizedExitCode
            else -> error("no exit code found")
        }
    }
}
