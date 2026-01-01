package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.subcommands.GitProcessingSubcommandExecutor

interface SubcommandExecutor {
    val subcommand: String
    fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int

    companion object {
        fun construct(
            mainArgs: MainArgs,
            gitProcessManagerFactoryInj: GitProcessManagerFactory? = null,
            reposInj: List<Repo>? = null,
        ): SubcommandExecutor {
            requireNotNull(mainArgs.subCommand) { "subcommand should be passed" }

            return GitProcessingSubcommandExecutor(
                mainArgs.subCommand,
                gitProcessManagerFactoryInj,
                reposInj,
            )
        }
    }
}
