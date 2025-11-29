package jp.unaguna.massgit

import jp.unaguna.massgit.printfilter.LineHeadFilter
import jp.unaguna.massgit.printmanager.PrintManagerThrough
import kotlin.concurrent.thread

class GitProcessManager(
    private val gitSubCommand: String,
    private val gitSubCommandArgs: List<String>,
) {
    fun run() {
        val args = mutableListOf("git", gitSubCommand).apply {
            addAll(gitSubCommandArgs)
        }
        val processBuilder = ProcessBuilder(args).apply {
            redirectErrorStream(false)
        }
        val process = processBuilder.start()

        val processController = ProcessController(
            process = process,
            printManager = PrintManagerThrough(
                LineHeadFilter("ping: ")
            ),
        )

        val thread = thread {
            processController.readOutput()
        }
        thread.join()
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val args = MainArgs.of(listOf("-x", "diff"))
            GitProcessManager(args.subCommand!!, args.subOptions).run()
        }
    }
}
