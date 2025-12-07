package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.SummaryPrinter
import jp.unaguna.massgit.common.collection.Either
import java.util.concurrent.Future

class RegularSummaryPrinter : SummaryPrinter {
    override fun printSummary(futures: List<Future<Either<Process, Throwable>>>) {
        val results = futures.map { future -> future.get() }
        val succeeded = results.count { it.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        System.err.println("Success: $succeeded, Failed: $failed, Total: ${results.size}")
    }
}
