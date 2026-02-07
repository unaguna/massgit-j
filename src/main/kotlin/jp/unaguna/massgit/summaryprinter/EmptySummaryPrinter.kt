package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.SummaryPrinter
import jp.unaguna.massgit.common.collection.Either
import org.slf4j.LoggerFactory

/**
 * Summary printer to output only into logging, not into stderr.
 */
class EmptySummaryPrinter<An> : SummaryPrinter<An> {
    override fun printSummary(results: List<Pair<Either<Process, Throwable>, An?>>) {
        val succeeded = results.count { it.first.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        logger.info("Success: {}, Failed: {}, Total: {}", succeeded, failed, results.size)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(EmptySummaryPrinter::class.java) }
    }
}
