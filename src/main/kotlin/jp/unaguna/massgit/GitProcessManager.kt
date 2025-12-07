package jp.unaguna.massgit

import jp.unaguna.massgit.common.collection.ClosablePair
import jp.unaguna.massgit.common.collection.getEither
import jp.unaguna.massgit.common.collection.submitForEach
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.GitProcessCanceledException
import jp.unaguna.massgit.exception.MassgitException
import jp.unaguna.massgit.exception.RepoNotContainUrlException
import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class GitProcessManagerBase {
    protected abstract val cmdTemplate: ProcessArgs
    protected open val printPostSummary: Boolean = true
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

    fun run(repos: List<Repo>, massgitBaseDir: Path? = null) {
        require(repos.isNotEmpty())

        // TODO: 同時に実行するスレッド数を指定できるようにする
        val executor = Executors.newFixedThreadPool(1)

        val executionFutures = repos.submitForEach(executor) { repo ->
            val errorFilter = errorFilter(repo)
            runCatching {
                val processBuilder = runCatching {
                    ProcessBuilder(cmdTemplate.render(repo)).apply {
                        if (massgitBaseDir != null) {
                            directory(massgitBaseDir.toFile())
                        }
                    }
                }.getOrElse { t -> throw GitProcessCanceledException(null, t) }

                val process = processBuilder.start()
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
                // TODO: 例外をログ出力
            }.getEither()
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }

        // Print Summary
        if (printPostSummary) {
            val results = executionFutures.map { future -> future.get() }
            val succeeded = results.count { it.isLeftAnd { p -> p.exitValue() == 0 } }
            val failed = results.size - succeeded

            System.err.println("Success: $succeeded, Failed: $failed, Total: ${results.size}")
        }
    }

    private fun createPrintManagers(repo: Repo, errorFilter: PrintFilter): ClosablePair<PrintManager, PrintManager> {
        return ClosablePair.of(
            { createPrintManager(repo) },
            { createPrintErrorManager(errorFilter) }
        )
    }
}

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repSuffix: String? = null,
) : GitProcessManagerBase() {
    override val printPostSummary = false
    override val cmdTemplate = buildProcessArgs {
        append("git")
        append("-C")
        append { r -> listOf(r.dirname) }
        append(gitSubCommand)
        append(gitSubCommandArgs)
    }

    override fun createPrintManager(repo: Repo): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}${repSuffix ?: ": "}")
        )
    }
}

class CloneProcessManager(
    private val repSuffix: String? = null,
) : GitProcessManagerBase() {
    override val cmdTemplate = buildProcessArgs {
        append("git")
        append("clone")
        append { r ->
            val url = r.url
                ?: throw RepoNotContainUrlException(r.dirname)
            listOf(url, r.dirname)
        }
    }

    override fun createPrintManager(repo: Repo): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}${repSuffix ?: ": "}")
        )
    }
}
