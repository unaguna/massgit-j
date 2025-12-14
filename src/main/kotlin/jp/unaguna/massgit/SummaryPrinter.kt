package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either
import java.util.concurrent.Future

interface SummaryPrinter {
    fun printSummary(futures: List<Future<Either<Process, Throwable>>>)
}
