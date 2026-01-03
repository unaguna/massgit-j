package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.GitProcessManager
import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposLoader
import org.slf4j.LoggerFactory

class GitProcessingSubcommandExecutor(
    override val subcommand: Subcommand,
    private val gitProcessManager: GitProcessManager,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val (_, reposFiltered) = ReposLoader(reposInj).load(conf)
        logger.debug("Repos filtered: {}", reposFiltered)

        return gitProcessManager.run(reposFiltered, massgitBaseDir = conf.massProjectDir)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(GitProcessingSubcommandExecutor::class.java) }
    }
}
