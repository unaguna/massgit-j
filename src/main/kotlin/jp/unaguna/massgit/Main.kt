package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo

@Suppress("UtilityClassWithPublicConstructor")
class Main {
    companion object {
        @Suppress("MemberNameEqualsClassName")
        @JvmStatic
        fun main(args: Array<String>) {
            val mainArgs = MainArgs.of(args)
            val conf = MainConfigurations(mainArgs)
            val repos = Repo.loadFromFile(conf.reposFilePath)

            // TODO: massgit 独自サブコマンドの場合の分岐を作る
            if (mainArgs.subCommand != null) {
                mainRunGitProcesses(
                    mainArgs.subCommand,
                    mainArgs.subOptions,
                    conf,
                    repos,
                )
            }
        }

        fun mainRunGitProcesses(
            gitSubCommand: String,
            gitSubCommandOptions: List<String>,
            conf: MainConfigurations,
            repos: List<Repo>,
        ) {
            // TODO: マーカーによる絞り込み
            val repoDirectories = repos.map { it.dirname }

            GitProcessManager(
                gitSubCommand,
                gitSubCommandOptions,
                repoDirectories = repoDirectories,
                repSuffix = conf.repSuffix,
            )
                .run(massgitBaseDir = conf.massProjectDir)
        }
    }
}
