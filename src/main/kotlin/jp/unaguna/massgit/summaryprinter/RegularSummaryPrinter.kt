package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.SummaryPrinter
import jp.unaguna.massgit.common.collection.Either

class RegularSummaryPrinter : SummaryPrinter {
    override fun printSummary(results: List<Either<Process, Throwable>>) {
        val succeeded = results.count { it.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        System.err.println("Success: $succeeded, Failed: $failed, Total: ${results.size}")
    }
}
