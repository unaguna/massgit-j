package jp.unaguna.massgit

interface SubcommandExecutor {
    val subcommand: String
    fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int
}
