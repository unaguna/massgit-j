package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

abstract class GitProcessManagerBase {
    protected abstract val cmdTemplate: ProcessArgs
    protected abstract fun createPrintManager(repo: Repo): PrintManager

    protected open fun createPrintErrorManager(repo: Repo): PrintManager {
        return PrintManagerThrough(
            LineHeadFilter("${repo.dirname}: "),
            out = System.err,
        )
    }

    fun run(repos: List<Repo>, massgitBaseDir: Path? = null) {
        require(repos.isNotEmpty())

        // TODO: 同時に実行するスレッド数を指定できるようにする
        val executor = Executors.newFixedThreadPool(1)

        repos.map { repo ->
            val processBuilder = ProcessBuilder(cmdTemplate.render(repo)).apply {
                if (massgitBaseDir != null) {
                    directory(massgitBaseDir.toFile())
                }
            }

            executor.submit {
                val process = processBuilder.start()
                createPrintManager(repo).use { printManager ->
                    createPrintErrorManager(repo).use { printErrorManager ->
                        val processController = ProcessController(
                            process = process,
                            printManager = printManager,
                            printErrorManager = printErrorManager,
                        )

                        processController.readOutput()
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }
    }
}

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repSuffix: String? = null,
): GitProcessManagerBase() {
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
