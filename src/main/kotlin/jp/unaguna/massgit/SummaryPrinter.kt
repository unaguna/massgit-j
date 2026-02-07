package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.Either

interface SummaryPrinter<An> {
    fun printSummary(results: List<Pair<Either<Process, Throwable>, An?>>)
}
