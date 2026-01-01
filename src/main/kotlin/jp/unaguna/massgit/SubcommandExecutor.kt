package jp.unaguna.massgit

interface SubcommandExecutor {
    val subcommand: Subcommand
    fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int
}
