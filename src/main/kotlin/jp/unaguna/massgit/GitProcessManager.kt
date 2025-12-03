package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repos: List<Repo>,
    private val repSuffix: String? = null,
) {
    private val cmdTemplate = buildProcessArgs {
        append("git")
        append("-C")
        append { r -> listOf(r.dirname) }
        append(gitSubCommand)
        append(gitSubCommandArgs)
    }

    fun run(massgitBaseDir: Path? = null) {
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
                PrintManagerThrough(
                    LineHeadFilter("${repo.dirname}${repSuffix ?: ": "}")
                ).use { printManager ->
                    PrintManagerThrough(
                        LineHeadFilter("${repo.dirname}: "),
                        out = System.err,
                    ).use { printErrorManager ->
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
