package jp.unaguna.massgit

import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repoDirectories: List<String>,
    private val repSuffix: String? = null,
) {
    fun run(massgitBaseDir: Path? = null) {
        require(repoDirectories.isNotEmpty())

        // TODO: 同時に実行するスレッド数を指定できるようにする
        val executor = Executors.newFixedThreadPool(1)

        repoDirectories.map { dirname ->
            val args = mutableListOf("git", "-C", dirname, gitSubCommand).apply {
                addAll(gitSubCommandArgs)
            }
            val processBuilder = ProcessBuilder(args).apply {
                redirectError(ProcessBuilder.Redirect.INHERIT)
                if (massgitBaseDir != null) {
                    directory(massgitBaseDir.toFile())
                }
            }

            executor.submit {
                val process = processBuilder.start()
                PrintManagerThrough(
                    LineHeadFilter("$dirname${repSuffix ?: ": "}")
                )
                    .use { printManager ->
                        val processController = ProcessController(
                            process = process,
                            printManager = printManager,
                        )

                        processController.readOutput()
                    }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            executor.awaitTermination(1, TimeUnit.MINUTES)
        }
    }
}
