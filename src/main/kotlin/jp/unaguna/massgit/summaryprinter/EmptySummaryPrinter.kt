package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.SummaryPrinter
import jp.unaguna.massgit.common.collection.Either
import org.slf4j.LoggerFactory

/**
 * Summary printer to output only into logging, not into stderr.
 */
class EmptySummaryPrinter : SummaryPrinter {
    override fun printSummary(results: List<Either<Process, Throwable>>) {
        val succeeded = results.count { it.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        logger.info("Success: {}, Failed: {}, Total: {}", succeeded, failed, results.size)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(EmptySummaryPrinter::class.java) }
    }
}
