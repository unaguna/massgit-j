package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.GitProcessResult
import jp.unaguna.massgit.SummaryPrinter
import org.slf4j.LoggerFactory

/**
 * Summary printer to output only into logging, not into stderr.
 */
class EmptySummaryPrinter<An> : SummaryPrinter<An> {
    override fun printSummary(results: List<GitProcessResult<An>>) {
        val succeeded = results.count { it.process.isLeftAnd { p -> p.exitValue() == 0 } }
        val failed = results.size - succeeded

        logger.info("Success: {}, Failed: {}, Total: {}", succeeded, failed, results.size)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(EmptySummaryPrinter::class.java) }
    }
}
