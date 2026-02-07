package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either

interface ExitCodeDecider<An> {
    fun decideExitCode(results: List<Pair<Either<Process, Throwable>, An?>>): Int
}
