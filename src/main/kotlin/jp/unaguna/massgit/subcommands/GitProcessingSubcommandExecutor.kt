package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.GitProcessManager
import jp.unaguna.massgit.GitProcessManagerFactory
import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.ProcessExecutor
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import org.slf4j.LoggerFactory

class GitProcessingSubcommandExecutor(
    override val subcommand: String,
    private val processExecutor: ProcessExecutor? = null,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val gitProcessManagerFactory = GitProcessManagerFactoryImpl(mainArgs, conf, processExecutor)
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
    private val processExecutor: ProcessExecutor? = null,
) : GitProcessManagerFactory {
    @Suppress("ThrowsCount")
    override fun create(): GitProcessManager {
        requireNotNull(mainArgs.subCommand) { "Subcommand not specified." }

        if (mainArgs.subCommand == Subcommand.MgClone) {
            return GitProcessManager.cloneAll(
                repSuffix = conf.repSuffix,
                processExecutor,
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

            return GitProcessManager.regular(mainArgs, processExecutor)
        }
    }
}

private class ProhibitedSubcommandException(subcommand: Subcommand) :
    MassgitException("subcommand '${subcommand.name}' is prohibited")

private class UnknownSubcommandException(subcommand: Subcommand) :
    MassgitException("unknown subcommand '${subcommand.name}'")

private class NoRepositoriesRegisteredException :
    MassgitException("No target repositories have been registered.")

private class NoRepositoriesTargetedException :
    MassgitException("No repositories were targeted.")
