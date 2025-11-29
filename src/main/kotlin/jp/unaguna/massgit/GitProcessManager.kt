package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import java.nio.file.Path
import kotlin.concurrent.thread
import kotlin.io.path.Path

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
    private val repoDirectories: List<String>,
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

            val processController = ProcessController(
                process = process,
                printManager = PrintManagerThrough(
                    LineHeadFilter("$dirname: ")
                ),
            )

            thread {
                processController.readOutput()
            }
        }
        threads.forEach { it.join() }
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val massgitBaseDir = System.getProperty("jp.unaguna.massgit.projectDir")?.let { Path(it) }
                ?: Path("").toAbsolutePath()
            val repos = Repo.loadFromFile(massgitBaseDir.resolve(".massgit").resolve("repos.json"))
            val repoDirectories = repos.map { it.dirname }

            val args = MainArgs.of(listOf("-x", "grep", "gradle"))
            GitProcessManager(
                args.subCommand!!,
                args.subOptions,
                repoDirectories = repoDirectories,
            ).run(massgitBaseDir)
        }
    }
}
