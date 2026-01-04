package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.GitProcessManager
import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposLoader
import jp.unaguna.massgit.help.HelpDefinition
import org.slf4j.LoggerFactory

class GitProcessingSubcommandExecutor(
    override val subcommand: Subcommand,
    private val gitProcessManager: GitProcessManager,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        if (mainArgs.subOptions.contains("--help")) {
            val result = printHelpIfExists()
            return if (result) {
                0
            } else {
                System.err.println("cannot use --help for 'massgit $subcommand'.")
                @Suppress("MagicNumber")
                127
            }
        }

        val (_, reposFiltered) = ReposLoader(reposInj).load(conf)
        logger.debug("Repos filtered: {}", reposFiltered)

        return gitProcessManager.run(reposFiltered, massgitBaseDir = conf.massProjectDir)
    }

    private fun printHelpIfExists(): Boolean {
        val helpDef = HelpDefinition.load()
        return when (val helpDefSub = helpDef.getSubcommandOrNull(subcommand.name)) {
            null -> false
            else -> {
                helpDefSub.print(System.out, "massgit")
                true
            }
        }
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(GitProcessingSubcommandExecutor::class.java) }
    }
}
