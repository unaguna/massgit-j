package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.ClosablePair
import jp.unaguna.massgit.common.collection.Either
import jp.unaguna.massgit.common.collection.containsAny
import jp.unaguna.massgit.common.collection.getEither
import jp.unaguna.massgit.common.collection.submitForEach
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.GitProcessCanceledException
import jp.unaguna.massgit.exception.MassgitException
import jp.unaguna.massgit.exception.RepoNotContainUrlException
import jp.unaguna.massgit.exitcode.GrepExitCodeDecider
import jp.unaguna.massgit.exitcode.RegularExitCodeDecider
import jp.unaguna.massgit.printfilter.DoNothingFilter
import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import jp.unaguna.massgit.summaryprinter.EmptySummaryPrinter
import jp.unaguna.massgit.summaryprinter.PullSummaryPrinter
import jp.unaguna.massgit.summaryprinter.RegularSummaryPrinter
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface GitProcessManager {
    fun run(repos: List<Repo>, massgitBaseDir: Path? = null): Int
}

abstract class GitProcessManagerBase<An>(
    private val processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManager {
    private val logger = LoggerFactory.getLogger(GitProcessManagerBase::class.java)
    protected abstract val cmdTemplate: ProcessArgs
    protected open val summaryPrinter: SummaryPrinter<An> = EmptySummaryPrinter()
    protected abstract val exitCodeDecider: ExitCodeDecider<An>
    protected abstract val outputAnalyzerFactory: OutputAnalyzerFactory<Repo, An>
    protected abstract fun createPrintManager(repo: Repo, outputAnalyzer: OutputAnalyzer<An>?): PrintManager

    private fun createPrintErrorManager(outputAnalyzer: OutputAnalyzer<An>?, errorFilter: PrintFilter): PrintManager {
        return PrintManagerThrough(
            errorFilter,
            outputAnalyzer = outputAnalyzer?.toStderrAdapter(),
            out = System.err,
        )
    }

    private fun errorFilter(repo: Repo): PrintFilter {
        return LineHeadFilter("${repo.dirname}: ")
    }

    override fun run(repos: List<Repo>, massgitBaseDir: Path?): Int {
        require(repos.isNotEmpty())

        // TODO: 同時に実行するスレッド数を指定できるようにする
        val executor = Executors.newFixedThreadPool(1)

        val executionFutures = repos.submitForEach(executor) { repo ->
            logger.trace("Start thread for {}", repo.dirname)

            val outputAnalyzer = outputAnalyzerFactory.create(repo)
            val errorFilter = errorFilter(repo)
            val threadResult = runCatching {
                val process = runCatching {
                    processExecutor.execute(
                        cmdTemplate.render(repo),
                        workingDir = massgitBaseDir,
                    )
                }.getOrElse { t -> throw GitProcessCanceledException(null, t) }

                createPrintManagers(repo, outputAnalyzer, errorFilter).use { (printManager, printErrorManager) ->
                    val processController = ProcessController(
                        process = process,
                        printManager = printManager,
                        printErrorManager = printErrorManager,
                    )

                    processController.readOutput()
                }
                process.waitFor()
                process
            }.onFailure { e ->
                val baseMsg = if (e is MassgitException) {
                    e.consoleMessage
                } else if (e.message != null) {
                    "some error occurred: ${e.message}"
                } else {
                    "some error occurred"
                }
                val message = errorFilter.mapLine(baseMsg)

                System.err.println(message)
                logger.error(message, e)
            }.getEither()

            logger.trace("End thread for {}; result={}", repo.dirname, threadResult)
            GitProcessResult(threadResult, outputAnalyzer.getResult())
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }

        val executionResults = executionFutures.map { future -> future.get() }
        summaryPrinter.printSummary(executionResults)
        return exitCodeDecider.decideExitCode(executionResults)
    }

    private fun createPrintManagers(
        repo: Repo,
        outputAnalyzer: OutputAnalyzer<An>?,
        errorFilter: PrintFilter,
    ): ClosablePair<PrintManager, PrintManager> {
        return ClosablePair.of(
            { createPrintManager(repo, outputAnalyzer) },
            { createPrintErrorManager(outputAnalyzer, errorFilter) }
        )
    }

    companion object {
        const val REP_SUFFIX_DEFAULT = ": "
        const val REP_SUFFIX_PATH_SEP = "/"
    }
}

data class GitProcessResult<An>(
    val process: Either<Process, Throwable>,
    val analysis: An,
)

abstract class GitProcessRegularManagerAbstract<An>(
    protected val mainArgs: MainArgs,
    protected val gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManagerBase<An>(processExecutor) {
    final override val cmdTemplate = buildProcessArgs {
        requireNotNull(mainArgs.subCommand)

        append("git")
        append("-C")
        append { r -> listOf(r.dirname) }
        for (gitConfig in gitConfigurations) {
            append("-c")
            append(gitConfig.toStringAsArg())
        }
        append(mainArgs.subCommand.name)
        append(mainArgs.subOptions)
    }

    open val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_DEFAULT
    override val summaryPrinter: SummaryPrinter<An> = RegularSummaryPrinter()
    override val exitCodeDecider: ExitCodeDecider<An> = RegularExitCodeDecider()

    override fun createPrintManager(repo: Repo, outputAnalyzer: OutputAnalyzer<An>?): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}$repSuffix"),
            outputAnalyzer = outputAnalyzer?.toStdoutAdapter(),
        )
    }
}

class GitProcessRegularManager(
    mainArgs: MainArgs,
    gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManagerAbstract<Unit>(mainArgs, gitConfigurations, processExecutor) {
    override val outputAnalyzerFactory = OutputAnalyzerDoNothingFactory<Repo>()
}

class GitProcessPullManager(
    mainArgs: MainArgs,
    gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManagerAbstract<GitProcessPullManager.PullOutputAnalysis>(
    mainArgs,
    gitConfigurations,
    processExecutor,
) {
    override val summaryPrinter: SummaryPrinter<PullOutputAnalysis> = PullSummaryPrinter()
    override val outputAnalyzerFactory: OutputAnalyzerFactory<Repo, PullOutputAnalysis> = PullOutputAnalyzerFactory()

    private class PullOutputAnalyzerFactory : OutputAnalyzerFactory<Repo, PullOutputAnalysis> {
        override fun create(repo: Repo): OutputAnalyzer<PullOutputAnalysis> {
            return PullOutputAnalyzer()
        }
    }

    private class PullOutputAnalyzer : OutputAnalyzer<PullOutputAnalysis> {
        var upToDate = false

        override fun loadStdoutLine(line: String) {
            if (line.startsWith("Already up to date")) {
                upToDate = true
            }
        }

        override fun loadStderrLine(line: String) {
            // do nothing
        }

        override fun getResult(): PullOutputAnalysis {
            return PullOutputAnalysis(
                isAlreadyUpToDate = upToDate,
            )
        }
    }

    data class PullOutputAnalysis(
        val isAlreadyUpToDate: Boolean,
    )
}

class GitProcessDiffManager(
    mainArgs: MainArgs,
    gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManagerAbstract<Unit>(mainArgs, gitConfigurations, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: when {
        mainArgs.subOptions.contains("--name-only") -> REP_SUFFIX_PATH_SEP
        else -> REP_SUFFIX_DEFAULT
    }
    override val summaryPrinter = EmptySummaryPrinter<Unit>()

    override val outputAnalyzerFactory = OutputAnalyzerDoNothingFactory<Repo>()

    override fun createPrintManager(repo: Repo, outputAnalyzer: OutputAnalyzer<Unit>?): PrintManager = when {
        mainArgs.subOptions.containsAny(
            "--name-only",
            "--numstat",
            "--shortstat",
            "--raw",
            "--name-status"
        ) -> PrintManagerThrough(
            LineHeadFilter("${repo.dirname}$repSuffix"),
            outputAnalyzer = outputAnalyzer?.toStdoutAdapter(),
        )
        else -> PrintManagerThrough(
            DoNothingFilter,
            outputAnalyzer = outputAnalyzer?.toStdoutAdapter(),
            header = "${repo.dirname}$repSuffix"
        )
    }
}

class GitProcessFilepathManager(
    mainArgs: MainArgs,
    gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManagerAbstract<Unit>(mainArgs, gitConfigurations, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_PATH_SEP
    override val summaryPrinter = EmptySummaryPrinter<Unit>()
    override val outputAnalyzerFactory = OutputAnalyzerDoNothingFactory<Repo>()
}

class GitProcessGrepManager(
    mainArgs: MainArgs,
    gitConfigurations: List<GitConfig>,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManagerAbstract<Unit>(mainArgs, gitConfigurations, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_PATH_SEP
    override val summaryPrinter = EmptySummaryPrinter<Unit>()
    override val exitCodeDecider: ExitCodeDecider<Unit> = GrepExitCodeDecider()
    override val outputAnalyzerFactory = OutputAnalyzerDoNothingFactory<Repo>()
}

class CloneProcessManager(
    private val repSuffix: String? = null,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManagerBase<Unit>(processExecutor) {
    override val cmdTemplate = buildProcessArgs {
        append("git")
        append("clone")
        append { r ->
            val url = r.url
                ?: throw RepoNotContainUrlException(r.dirname)
            listOf(url, r.dirname)
        }
    }

    override val summaryPrinter = RegularSummaryPrinter<Unit>()
    override val exitCodeDecider = RegularExitCodeDecider<Unit>()
    override val outputAnalyzerFactory = OutputAnalyzerDoNothingFactory<Repo>()

    override fun createPrintManager(repo: Repo, outputAnalyzer: OutputAnalyzer<Unit>?): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}${repSuffix ?: REP_SUFFIX_DEFAULT}"),
            outputAnalyzer = outputAnalyzer?.toStdoutAdapter(),
        )
    }
}
