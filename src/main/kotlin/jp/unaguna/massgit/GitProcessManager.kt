package jp.unaguna.massgit

import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerStoreAll
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import kotlin.concurrent.thread

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repoDirectories: List<String>,
    private val repSuffix: String? = null,
) {
    fun run(massgitBaseDir: Path? = null) {
        require(repoDirectories.isNotEmpty())

        val threads = repoDirectories.map { dirname ->
            val args = mutableListOf("git", "-C", dirname, gitSubCommand).apply {
                addAll(gitSubCommandArgs)
            }
            val processBuilder = ProcessBuilder(args).apply {
                redirectError(ProcessBuilder.Redirect.INHERIT)
                if (massgitBaseDir != null) {
                    directory(massgitBaseDir.toFile())
                }
            }
            val process = processBuilder.start()

            thread {
                PrintManagerStoreAll(
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
        threads.forEach { it.join() }
    }
}
