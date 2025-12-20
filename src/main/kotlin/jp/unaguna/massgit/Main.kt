package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import kotlin.system.exitProcess

class Main {
    fun run(
        mainArgs: MainArgs,
        confInj: MainConfigurations? = null,
        reposInj: List<Repo>? = null,
        gitProcessManagerFactoryInj: GitProcessManagerFactory? = null
    ) {
        if (mainArgs.mainOptions.isVersion()) {
            showVersion()
            return
        }

        val conf = confInj ?: MainConfigurations(mainArgs.mainOptions)
        val repos = reposInj ?: runCatching {
            Repo.loadFromFile(conf.reposFilePath)
        }.getOrElse { t -> throw LoadingReposFailedException(t) }

        val reposFiltered = when (val markerConditions = conf.markerConditions) {
            null -> repos
            else -> repos.filter { markerConditions.satisfies(it.markers) }
        }

        val gitProcessManagerFactory = gitProcessManagerFactoryInj
            ?: GitProcessManagerFactoryImpl(mainArgs, conf)
        val gitProcessManager = gitProcessManagerFactory.create()
        gitProcessManager.run(reposFiltered, massgitBaseDir = conf.massProjectDir)
    }

    private fun showVersion() {
        val version = VersionProperties.getVersion()
        println("massgit on java $version")
    }

    companion object {
        @Suppress("MagicNumber", "MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            runCatching {
                Main().run(mainArgs = MainArgs.of(args))
            }.onFailure { e ->
                val message = if (e is MassgitException) {
                    e.consoleMessage
                } else if (e.message != null) {
                    "some error occurred: ${e.message}"
                } else {
                    "some error occurred"
                }

                System.err.println(message)
                // TODO: 例外をログ出力

                exitProcess(127)
            }
            // TODO: 実行結果によって終了コードを変える
            exitProcess(0)
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
