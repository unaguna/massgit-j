package jp.unaguna.massgit.exception

abstract class MassgitException(msg: String, cause: Throwable?) : Exception(msg, cause), ConsoleMessage {
    constructor(msg: String) : this(msg, null)

    override val consoleMessage = chainMessage(msg, cause)

    companion object {
        fun chainMessage(msg: String, cause: Throwable?): String {
            return if (cause is MassgitException) {
                msg + ": " + cause.consoleMessage
            } else {
                msg
            }
        }
    }
}

class RepoNotContainUrlException(repoName: String) :
    MassgitException("URL for repo '$repoName' is not specified")

class GitProcessCanceledException(command: List<String>?, cause: Throwable?) :
    MassgitException("canceled to run the git process " + (command?.toString() ?: ""), cause) {

    override val consoleMessage: String = chainMessage("canceled to run the git process", cause)
}

class LoadingReposFailedException(cause: Throwable) :
    MassgitException("failed to load repos file", cause)
