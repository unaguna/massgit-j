package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either

interface ExitCodeDecider {
    fun decideExitCode(results: List<Either<Process, Throwable>>): Int
}
