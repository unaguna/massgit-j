package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.GitProcessManager
import jp.unaguna.massgit.GitProcessManagerFactory
import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import org.slf4j.LoggerFactory

class GitProcessingSubcommandExecutor(
    override val subcommand: String,
    private val gitProcessManagerFactoryInj: GitProcessManagerFactory? = null,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val gitProcessManagerFactory = gitProcessManagerFactoryInj
            ?: GitProcessManagerFactoryImpl(mainArgs, conf)
        val gitProcessManager = gitProcessManagerFactory.create()

        val repos = reposInj ?: runCatching {
            Repo.loadFromFile(conf.reposFilePath)
        }.getOrElse { t -> throw LoadingReposFailedException(t) }
        if (repos.isEmpty()) {
            throw NoRepositoriesRegisteredException()
        }
        val reposFiltered = when (val markerConditions = conf.markerConditions) {
            null -> repos
            else -> repos.filter { markerConditions.satisfies(it.markers) }
        }
        if (reposFiltered.isEmpty()) {
            throw NoRepositoriesTargetedException()
        }
        logger.debug("Repos filtered: {}", reposFiltered)

        return gitProcessManager.run(reposFiltered, massgitBaseDir = conf.massProjectDir)
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(GitProcessingSubcommandExecutor::class.java) }
    }
}

private class GitProcessManagerFactoryImpl(
    private val mainArgs: MainArgs,
    private val conf: MainConfigurations,
) : GitProcessManagerFactory {
    @Suppress("ThrowsCount")
    override fun create(): GitProcessManager {
        requireNotNull(mainArgs.subCommand) {
            throw UnknownSubcommandException("")
        }

        if (mainArgs.subCommand == "mg-clone") {
            return GitProcessManager.cloneAll(
                repSuffix = conf.repSuffix,
            )
        } else {
            when (conf.subcommandAcceptation(mainArgs.subCommand)) {
                MainConfigurations.SubcommandAcceptation.PROHIBITED -> {
                    throw ProhibitedSubcommandException(mainArgs.subCommand)
                }
                MainConfigurations.SubcommandAcceptation.UNKNOWN -> {
                    throw UnknownSubcommandException(mainArgs.subCommand)
                }
                MainConfigurations.SubcommandAcceptation.OK -> Unit
            }

            return GitProcessManager.regular(mainArgs)
        }
    }
}

private class ProhibitedSubcommandException(subcommand: String) :
    MassgitException("subcommand '$subcommand' is prohibited")

private class UnknownSubcommandException(subcommand: String) :
    MassgitException("unknown subcommand '$subcommand'")

private class NoRepositoriesRegisteredException :
    MassgitException("No target repositories have been registered.")

private class NoRepositoriesTargetedException :
    MassgitException("No repositories were targeted.")
