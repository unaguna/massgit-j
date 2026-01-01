package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.MassgitException
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

    class Other(name: String) : Subcommand(name) {
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
        ): GitProcessManager = when (conf.subcommandAcceptation(this)) {
            MainConfigurations.SubcommandAcceptation.PROHIBITED -> {
                throw ProhibitedSubcommandException(this)
            }
            MainConfigurations.SubcommandAcceptation.UNKNOWN -> {
                throw UnknownSubcommandException(this)
            }
            MainConfigurations.SubcommandAcceptation.OK -> when (mainArgs.subCommand?.name) {
                "diff" -> GitProcessDiffManager(mainArgs, processExecutor ?: ProcessExecutor.default())
                "grep" -> GitProcessGrepManager(mainArgs, processExecutor ?: ProcessExecutor.default())
                "ls-files" -> GitProcessFilepathManager(mainArgs, processExecutor ?: ProcessExecutor.default())
                else -> GitProcessRegularManager(mainArgs, processExecutor ?: ProcessExecutor.default())
            }
        }

        override fun equals(other: Any?): Boolean {
            return other is Other && other.name == this.name
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }

    companion object {
        fun of(subcommand: String): Subcommand {
            return when (subcommand) {
                "mg-clone" -> MgClone
                else -> Other(subcommand)
            }
        }
    }
}

private class ProhibitedSubcommandException(subcommand: Subcommand) :
    MassgitException("subcommand '${subcommand.name}' is prohibited")

private class UnknownSubcommandException(subcommand: Subcommand) :
    MassgitException("unknown subcommand '${subcommand.name}'")
