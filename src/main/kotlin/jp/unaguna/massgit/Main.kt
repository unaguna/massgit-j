package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo

class Main {
    companion object {
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
            )
                .run(massgitBaseDir = conf.massProjectDir)
        }
    }
}
