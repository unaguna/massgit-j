package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.SummaryPrinter
import jp.unaguna.massgit.common.collection.Either
import org.slf4j.LoggerFactory

class RegularSummaryPrinter<An> : SummaryPrinter<An> {
    override fun printSummary(results: List<Pair<Either<Process, Throwable>, An?>>) {
        val succeeded = results.count { it.first.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        val message = "Success: $succeeded, Failed: $failed, Total: ${results.size}"
        System.err.println(message)
        logger.info(message)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(RegularSummaryPrinter::class.java) }
    }
}
