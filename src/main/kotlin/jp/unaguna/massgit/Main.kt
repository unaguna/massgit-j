package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.SystemProp
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import jp.unaguna.massgit.logging.LoggingSetUp
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Main {
    private val logger = LoggerFactory.getLogger(Main::class.java)

    init {
        logger.info("Start massgit.")
    }

    fun run(
        mainArgs: MainArgs,
        confInj: MainConfigurations? = null,
        reposInj: List<Repo>? = null,
        gitProcessManagerFactoryInj: GitProcessManagerFactory? = null
    ): Int {
        if (mainArgs.mainOptions.isVersion()) {
            showVersion()
            return 0
        }

        val conf = confInj ?: MainConfigurations(mainArgs.mainOptions)

        val gitProcessManagerFactory = gitProcessManagerFactoryInj
            ?: GitProcessManagerFactoryImpl(mainArgs, conf)
        val gitProcessManager = gitProcessManagerFactory.create()

        val repos = reposInj ?: runCatching {
            Repo.loadFromFile(conf.reposFilePath)
        }.getOrElse { t -> throw LoadingReposFailedException(t) }
        val reposFiltered = when (val markerConditions = conf.markerConditions) {
            null -> repos
            else -> repos.filter { markerConditions.satisfies(it.markers) }
        }

        return gitProcessManager.run(reposFiltered, massgitBaseDir = conf.massProjectDir)
    }

    private fun showVersion() {
        val version = VersionProperties.getVersion()
        println("massgit on java $version")
    }

    companion object {
        @Suppress("MagicNumber", "MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            SystemProp.initialize()
            LoggingSetUp.setUpLogging()

            val mainInstance = Main()

            val exitCode = runCatching {
                mainInstance.run(mainArgs = MainArgs.of(args))
            }.onFailure { e ->
                val message = if (e is MassgitException) {
                    e.consoleMessage
                } else if (e.message != null) {
                    "some error occurred: ${e.message}"
                } else {
                    "some error occurred"
                }

                System.err.println(message)
                mainInstance.logger.warn(message, e)
            }.getOrDefault(127)

            exitProcess(exitCode)
        }
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
