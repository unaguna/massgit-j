package jp.unaguna.massgit.exitcode

import jp.unaguna.massgit.ExitCodeDecider
import jp.unaguna.massgit.common.collection.Either
import jp.unaguna.massgit.common.collection.groupByType
import java.util.concurrent.Future

class RegularExitCodeDecider : ExitCodeDecider {
    @Suppress("MagicNumber")
    override fun decideExitCode(futures: List<Future<Either<Process, Throwable>>>): Int {
        val results = futures.map { future -> future.get() }

        val (processes, throwable) = results.groupByType()
        val summarizedExitCode = processes.maxOfOrNull { p -> p.exitValue() }

        return when {
            throwable.isNotEmpty() -> 127
            summarizedExitCode != null -> summarizedExitCode
            else -> error("no exit code found")
        }
    }
}
