package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.MassgitException
import kotlin.system.exitProcess

@Suppress("UtilityClassWithPublicConstructor")
class Main {
    companion object {
        @Suppress("MagicNumber", "MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            runCatching {
                run(args)
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

        @Suppress("ThrowsCount")
        private fun run(args: Array<String>) {
            val mainArgs = MainArgs.of(args)

            if (mainArgs.mainOptions.isVersion()) {
                showVersion()
                return
            }

            val conf = MainConfigurations(mainArgs.mainOptions)
            val repos = runCatching {
                Repo.loadFromFile(conf.reposFilePath)
            }.getOrElse { t -> throw LoadingReposFailedException(t) }

            if (mainArgs.subCommand == "mg-clone") {
                runGitCloneProcesses(
                    conf,
                    repos,
                )
            } else if (mainArgs.subCommand != null) {
                when (conf.subcommandAcceptation(mainArgs.subCommand)) {
                    MainConfigurations.SubcommandAcceptation.PROHIBITED -> {
                        throw IllegalArgumentException("subcommand '${mainArgs.subCommand}' is prohibited")
                    }
                    MainConfigurations.SubcommandAcceptation.UNKNOWN -> {
                        throw IllegalArgumentException("unknown subcommand is specified: ${mainArgs.subCommand}")
                    }
                    MainConfigurations.SubcommandAcceptation.OK -> Unit
                }

                mainRunGitProcesses(
                    mainArgs,
                    conf,
                    repos,
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
            // TODO: repos のマーカーによる絞り込み

            GitProcessManager.construct(mainArgs)
                .run(repos, massgitBaseDir = conf.massProjectDir)
        }

        private fun runGitCloneProcesses(
            conf: MainConfigurations,
            repos: List<Repo>,
        ) {
            // TODO: repos のマーカーによる絞り込み

            CloneProcessManager(
                repSuffix = conf.repSuffix,
            )
                .run(repos, massgitBaseDir = conf.massProjectDir)
        }
    }
}
