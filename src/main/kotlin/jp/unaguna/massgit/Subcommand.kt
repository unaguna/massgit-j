package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.subcommands.GitProcessingSubcommandExecutor

sealed class Subcommand(val name: String) {
    abstract fun executor(
        mainArgs: MainArgs,
        conf: MainConfigurations,
        processExecutor: ProcessExecutor? = null,
        reposInj: List<Repo>? = null,
    ): SubcommandExecutor

    abstract fun gitProcessManager(
        mainArgs: MainArgs,
        conf: MainConfigurations,
        processExecutor: ProcessExecutor? = null,
    ): GitProcessManager

    override fun toString(): String {
        return name
    }

    object MgClone : Subcommand("mg-clone") {
        override fun executor(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?,
            reposInj: List<Repo>?,
        ): SubcommandExecutor {
            return GitProcessingSubcommandExecutor(
                this,
                this.gitProcessManager(mainArgs, conf, processExecutor),
                reposInj,
            )
        }

        override fun gitProcessManager(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?
        ): GitProcessManager {
            return CloneProcessManager(
                repSuffix = conf.repSuffix,
                processExecutor ?: ProcessExecutor.default(),
            )
        }
    }

    object Diff : GitSubcommand("diff") {
        override fun gitProcessManager(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?
        ): GitProcessManager {
            check(mainArgs.subCommand == this)
            return GitProcessDiffManager(mainArgs, processExecutor ?: ProcessExecutor.default())
        }
    }

    object Grep : GitSubcommand("grep") {
        override fun gitProcessManager(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?
        ): GitProcessManager {
            check(mainArgs.subCommand == this)
            return GitProcessGrepManager(mainArgs, processExecutor ?: ProcessExecutor.default())
        }
    }

    object LsFiles : GitSubcommand("ls-files") {
        override fun gitProcessManager(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?
        ): GitProcessManager {
            check(mainArgs.subCommand == this)
            return GitProcessFilepathManager(mainArgs, processExecutor ?: ProcessExecutor.default())
        }
    }

    sealed class GitSubcommand(name: String) : Subcommand(name) {
        override fun executor(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?,
            reposInj: List<Repo>?,
        ) =
            GitProcessingSubcommandExecutor(
                this,
                this.gitProcessManager(mainArgs, conf, processExecutor),
                reposInj,
            )

        override fun gitProcessManager(
            mainArgs: MainArgs,
            conf: MainConfigurations,
            processExecutor: ProcessExecutor?
        ): GitProcessManager = GitProcessRegularManager(mainArgs, processExecutor ?: ProcessExecutor.default())
    }

    class OtherGitSubcommand(name: String) : GitSubcommand(name) {
        init {
            check(!fixedGitSubcommands.containsKey(name))
        }

        override fun equals(other: Any?): Boolean {
            return other is OtherGitSubcommand && other.name == this.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }

    companion object {
        private val fixedGitSubcommands = mapOf(
            Diff.name to Diff,
            Grep.name to Grep,
            LsFiles.name to LsFiles,
            // When new subcommand object is added here,
            // it must be added also in the tests:
            // 'MainArgsTest.testSubCommandObject()'
            // and 'SubcommandTest.testOtherGitSubcommandConstructionError()'.
        )

        fun of(subcommand: String): Subcommand {
            return when {
                subcommand == "mg-clone" -> MgClone
                fixedGitSubcommands.containsKey(subcommand) -> fixedGitSubcommands[subcommand]!!
                else -> OtherGitSubcommand(subcommand)
            }
        }
    }
}
