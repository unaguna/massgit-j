package jp.unaguna.massgit.exitcode

import jp.unaguna.massgit.ExitCodeDecider
import jp.unaguna.massgit.common.collection.Either
import jp.unaguna.massgit.common.collection.groupByType

class RegularExitCodeDecider<An> : ExitCodeDecider<An> {
    @Suppress("MagicNumber")
    override fun decideExitCode(results: List<Pair<Either<Process, Throwable>, An?>>): Int {
        val (processes, throwable) = results.map { it.first }.groupByType()
        val summarizedExitCode = processes.maxOfOrNull { p -> p.exitValue() }

        return when {
            throwable.isNotEmpty() -> 127
            summarizedExitCode != null -> summarizedExitCode
            else -> error("no exit code found")
        }
    }
}
