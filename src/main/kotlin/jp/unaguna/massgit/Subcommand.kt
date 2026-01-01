package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.subcommands.GitProcessingSubcommandExecutor

sealed class Subcommand(val name: String) {
    abstract fun executor(
        processExecutor: ProcessExecutor? = null,
        reposInj: List<Repo>? = null,
    ): SubcommandExecutor

    override fun toString(): String {
        return name
    }

    object MgClone : Subcommand("mg-clone") {
        override fun executor(processExecutor: ProcessExecutor?, reposInj: List<Repo>?) =
            GitProcessingSubcommandExecutor(
                name,
                processExecutor,
                reposInj,
            )
    }

    class Other(name: String) : Subcommand(name) {
        override fun executor(processExecutor: ProcessExecutor?, reposInj: List<Repo>?) =
            GitProcessingSubcommandExecutor(
                name,
                processExecutor,
                reposInj,
            )

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
