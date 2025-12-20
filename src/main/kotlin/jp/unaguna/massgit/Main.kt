package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import kotlin.system.exitProcess

class Main {
    @Suppress("ThrowsCount")
    fun run(
        mainArgs: MainArgs,
        confInj: MainConfigurations? = null,
        reposInj: List<Repo>? = null,
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

        if (mainArgs.subCommand == "mg-clone") {
            runGitCloneProcesses(
                conf,
                reposFiltered,
            )
        } else if (mainArgs.subCommand != null) {
            when (conf.subcommandAcceptation(mainArgs.subCommand)) {
                MainConfigurations.SubcommandAcceptation.PROHIBITED -> {
                    throw ProhibitedSubcommandException(mainArgs.subCommand)
                }
                MainConfigurations.SubcommandAcceptation.UNKNOWN -> {
                    throw UnknownSubcommandException(mainArgs.subCommand)
                }
                MainConfigurations.SubcommandAcceptation.OK -> Unit
            }

            mainRunGitProcesses(
                mainArgs,
                conf,
                reposFiltered,
            )
        }
    }

    private fun showVersion() {
        val version = VersionProperties.getVersion()
        println("massgit on java $version")
    }

    private fun mainRunGitProcesses(
        mainArgs: MainArgs,
        conf: MainConfigurations,
        repos: List<Repo>,
    ) {
        GitProcessRegularManager.construct(mainArgs)
            .run(repos, massgitBaseDir = conf.massProjectDir)
    }

    private fun runGitCloneProcesses(
        conf: MainConfigurations,
        repos: List<Repo>,
    ) {
        CloneProcessManager(
            repSuffix = conf.repSuffix,
        )
            .run(repos, massgitBaseDir = conf.massProjectDir)
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

private class ProhibitedSubcommandException(subcommand: String) :
    MassgitException("subcommand '$subcommand' is prohibited")

private class UnknownSubcommandException(subcommand: String) :
    MassgitException("unknown subcommand '$subcommand'")
