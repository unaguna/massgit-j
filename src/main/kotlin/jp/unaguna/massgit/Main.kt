package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo

@Suppress("UtilityClassWithPublicConstructor")
class Main {
    companion object {
        @Suppress("MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            val mainArgs = MainArgs.of(args)

            if (mainArgs.mainOptions.contains(MainArgs.OptionDef.VERSION)) {
                showVersion()
                return
            }

            val conf = MainConfigurations(mainArgs.mainOptions)
            val repos = Repo.loadFromFile(conf.reposFilePath)

            if (mainArgs.subCommand == "mg-clone") {
                runGitCloneProcesses(
                    conf,
                    repos,
                )
            } else if (mainArgs.subCommand != null) {
                require(!conf.prohibitSubcommand(mainArgs.subCommand)) {
                    "subcommand '${mainArgs.subCommand}' is prohibited"
                }

                mainRunGitProcesses(
                    mainArgs.subCommand,
                    mainArgs.subOptions,
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
            gitSubCommand: String,
            gitSubCommandOptions: List<String>,
            conf: MainConfigurations,
            repos: List<Repo>,
        ) {
            // TODO: repos のマーカーによる絞り込み

            GitProcessManager(
                gitSubCommand,
                gitSubCommandOptions,
                repSuffix = conf.repSuffix,
            )
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
