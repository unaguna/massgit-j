package jp.unaguna.massgit.exitcode

import jp.unaguna.massgit.ExitCodeDecider
import jp.unaguna.massgit.common.collection.Either
import jp.unaguna.massgit.common.collection.groupByType

class GrepExitCodeDecider : ExitCodeDecider {
    @Suppress("MagicNumber")
    override fun decideExitCode(results: List<Either<Process, Throwable>>): Int {
        val (processes, throwable) = results.groupByType()
        val exitCodes = processes.map { p -> p.exitValue() }.toSet()

        // If the processes at least one return 0 (found), this doesn't return 1; '1' means 'not found', not 'error'.
        // If the processes at least one return 2 or more (error), this returns 2 or more as usual.
        return when {
            throwable.isNotEmpty() -> 127
            exitCodes == setOf(1) -> 1
            exitCodes.isNotEmpty() -> exitCodes.asSequence().filter { it != 1 }.max()
            else -> error("no exit code found")
        }
    }
}
