package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.ClosablePair
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
import jp.unaguna.massgit.summaryprinter.RegularSummaryPrinter
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface GitProcessManager {
    fun run(repos: List<Repo>, massgitBaseDir: Path? = null): Int
}

abstract class GitProcessManagerBase(
    private val processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManager {
    private val logger = LoggerFactory.getLogger(GitProcessManagerBase::class.java)
    protected abstract val cmdTemplate: ProcessArgs
    protected open val summaryPrinter: SummaryPrinter = EmptySummaryPrinter()
    protected abstract val exitCodeDecider: ExitCodeDecider
    protected abstract fun createPrintManager(repo: Repo): PrintManager

    private fun createPrintErrorManager(errorFilter: PrintFilter): PrintManager {
        return PrintManagerThrough(
            errorFilter,
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

            val errorFilter = errorFilter(repo)
            val threadResult = runCatching {
                val process = runCatching {
                    processExecutor.execute(
                        cmdTemplate.render(repo),
                        workingDir = massgitBaseDir,
                    )
                }.getOrElse { t -> throw GitProcessCanceledException(null, t) }

                createPrintManagers(repo, errorFilter).use { (printManager, printErrorManager) ->
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
            threadResult
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }

        val executionResults = executionFutures.map { future -> future.get() }
        summaryPrinter.printSummary(executionResults)
        return exitCodeDecider.decideExitCode(executionResults)
    }

    private fun createPrintManagers(repo: Repo, errorFilter: PrintFilter): ClosablePair<PrintManager, PrintManager> {
        return ClosablePair.of(
            { createPrintManager(repo) },
            { createPrintErrorManager(errorFilter) }
        )
    }

    companion object {
        const val REP_SUFFIX_DEFAULT = ": "
        const val REP_SUFFIX_PATH_SEP = "/"
    }
}

open class GitProcessRegularManager protected constructor(
    protected val mainArgs: MainArgs,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManagerBase(processExecutor) {
    override val cmdTemplate = buildProcessArgs {
        requireNotNull(mainArgs.subCommand)

        append("git")
        append("-C")
        append { r -> listOf(r.dirname) }
        append(mainArgs.subCommand.name)
        append(mainArgs.subOptions)
    }

    open val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_DEFAULT
    override val summaryPrinter: SummaryPrinter = RegularSummaryPrinter()
    override val exitCodeDecider: ExitCodeDecider = RegularExitCodeDecider()

    override fun createPrintManager(repo: Repo): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}$repSuffix")
        )
    }

    companion object {
        fun construct(
            mainArgs: MainArgs,
            processExecutor: ProcessExecutor = ProcessExecutor.default(),
        ): GitProcessRegularManager {
            return when (mainArgs.subCommand?.name) {
                "diff" -> GitProcessDiffManager(mainArgs, processExecutor)
                "grep" -> GitProcessGrepManager(mainArgs, processExecutor)
                "ls-files" -> GitProcessFilepathManager(mainArgs, processExecutor)
                else -> GitProcessRegularManager(mainArgs, processExecutor)
            }
        }
    }
}

class GitProcessDiffManager(
    mainArgs: MainArgs,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManager(mainArgs, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: when {
        mainArgs.subOptions.contains("--name-only") -> REP_SUFFIX_PATH_SEP
        else -> REP_SUFFIX_DEFAULT
    }
    override val summaryPrinter = EmptySummaryPrinter()

    override fun createPrintManager(repo: Repo): PrintManager = when {
        mainArgs.subOptions.containsAny(
            "--name-only",
            "--numstat",
            "--shortstat",
            "--raw",
            "--name-status"
        ) -> PrintManagerThrough(
            LineHeadFilter("${repo.dirname}$repSuffix")
        )
        else -> PrintManagerThrough(
            DoNothingFilter,
            header = "${repo.dirname}$repSuffix"
        )
    }
}

class GitProcessFilepathManager(
    mainArgs: MainArgs,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManager(mainArgs, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_PATH_SEP
    override val summaryPrinter = EmptySummaryPrinter()
}

class GitProcessGrepManager(
    mainArgs: MainArgs,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessRegularManager(mainArgs, processExecutor) {
    override val repSuffix: String = mainArgs.mainOptions.getRepSuffix() ?: REP_SUFFIX_PATH_SEP
    override val summaryPrinter = EmptySummaryPrinter()
    override val exitCodeDecider: ExitCodeDecider = GrepExitCodeDecider()
}

class CloneProcessManager(
    private val repSuffix: String? = null,
    processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : GitProcessManagerBase(processExecutor) {
    override val cmdTemplate = buildProcessArgs {
        append("git")
        append("clone")
        append { r ->
            val url = r.url
                ?: throw RepoNotContainUrlException(r.dirname)
            listOf(url, r.dirname)
        }
    }

    override val summaryPrinter = RegularSummaryPrinter()
    override val exitCodeDecider = RegularExitCodeDecider()

    override fun createPrintManager(repo: Repo): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}${repSuffix ?: REP_SUFFIX_DEFAULT}")
        )
    }
}
