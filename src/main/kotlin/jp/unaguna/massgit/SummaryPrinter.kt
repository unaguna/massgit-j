package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either

interface SummaryPrinter {
    fun printSummary(results: List<Either<Process, Throwable>>)
}
