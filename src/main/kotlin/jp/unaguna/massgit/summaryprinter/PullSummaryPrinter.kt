package jp.unaguna.massgit.summaryprinter

import jp.unaguna.massgit.GitProcessPullManager
import jp.unaguna.massgit.GitProcessResult
import jp.unaguna.massgit.SummaryPrinter
import org.slf4j.LoggerFactory

class PullSummaryPrinter : SummaryPrinter<GitProcessPullManager.PullOutputAnalysis> {
    override fun printSummary(
        results: List<GitProcessResult<GitProcessPullManager.PullOutputAnalysis>>,
    ) {
        var successCount = 0
        var noOpCount = 0
        var failureCount = 0

        for ((result, analysis) in results) {
            when {
                result.isRight || result.isLeftAnd { p -> p.exitValue() != 0 } -> failureCount++
                analysis.isAlreadyUpToDate -> noOpCount++
                else -> successCount++
            }
        }

        val message = "Success: $successCount, Already UP-TO-DATE: $noOpCount, Failed: $failureCount, " +
            "Total: ${results.size}"
        System.err.println(message)
        logger.info(message)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(RegularSummaryPrinter::class.java) }
    }
}
