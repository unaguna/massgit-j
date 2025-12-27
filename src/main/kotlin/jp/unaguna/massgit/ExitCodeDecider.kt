package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either
import java.util.concurrent.Future

interface ExitCodeDecider {
    fun decideExitCode(futures: List<Future<Either<Process, Throwable>>>): Int
}
