package jp.unaguna.massgit

interface SummaryPrinter<An> {
    fun printSummary(results: List<GitProcessResult<An>>)
}
