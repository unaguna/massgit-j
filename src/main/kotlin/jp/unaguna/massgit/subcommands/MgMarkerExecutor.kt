package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.common.args.Option
import jp.unaguna.massgit.common.args.OptionDefProvider
import jp.unaguna.massgit.common.args.Options
import jp.unaguna.massgit.configfile.MarkerEditQuery
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposEditor
import jp.unaguna.massgit.configfile.ReposLoader
import jp.unaguna.massgit.exception.MassgitException
import org.slf4j.LoggerFactory
import java.util.Locale.getDefault

class MgMarkerExecutor(
    override val subcommand: Subcommand,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val (reposOriginal, reposFiltered) = ReposLoader(reposInj).load(conf)
        logger.debug("Repos original: {}", reposOriginal)
        logger.debug("Repos filtered: {}", reposFiltered)

        val modeStr = mainArgs.subOptions.getOrNull(0)
            ?: throw MgMarkerNullModeException()
        val mode = try {
            MgMarkerMode.valueOf(modeStr.uppercase(getDefault()))
        } catch (e: IllegalArgumentException) {
            throw IllegalMgMarkerModeException(modeStr, e)
        }

        val optionsForMode = mainArgs.subOptions.subList(1, mainArgs.subOptions.size)
        val (mgMarkerOptions, targetReposTmp) = MgMarkerOptions.build(optionsForMode)
        val targetRepos = targetReposTmp.ifEmpty { null }

        return when (mode) {
            MgMarkerMode.LIST -> listMarkers(reposOriginal, reposFiltered, mgMarkerOptions, targetRepos)
            MgMarkerMode.EDIT -> editMarkers(reposOriginal, reposFiltered, mgMarkerOptions, targetRepos, conf)
        }
    }

    private fun listMarkers(
        reposOriginal: List<Repo>,
        reposFiltered: List<Repo>,
        mgMarkerOptions: MgMarkerOptions,
        targetReposByArgs: List<String>?,
    ): Int {
        if (mgMarkerOptions.isNotEmpty()) {
            // TODO: 適切な Massgit 例外に置き換え
            error("'mg-marker list' cannot receive options")
        }

        val nonExistRepoNameSpecified = mutableListOf<String>()
        val targetRepos = if (targetReposByArgs != null) {
            val reposExistingName = reposOriginal.asSequence().map { it.dirname }.toSet()
            val reposFilteredNameMap = reposFiltered.associateBy { it.dirname }

            targetReposByArgs.mapNotNull { repoName ->
                reposFilteredNameMap[repoName].also { repo ->
                    if (repo == null && repoName !in reposExistingName) {
                        nonExistRepoNameSpecified.add(repoName)
                        logger.trace("Non-exist repository name is specified: {}", repoName)
                    }
                }
            }
        } else {
            reposFiltered
        }

        if (nonExistRepoNameSpecified.isNotEmpty()) {
            System.err.println(
                "warn: this massgit project doesn't contain some specified repository: " +
                    nonExistRepoNameSpecified.joinToString(",") { "'$it'" }
            )
        }

        for (repo in targetRepos) {
            println("${repo.dirname} ${repo.markers.joinToString(",")}")
        }
        return when {
            nonExistRepoNameSpecified.isNotEmpty() -> EXIT_CODE_NON_EXISTS_REPO_NAME_SPECIFIED
            else -> 0
        }
    }

    private fun editMarkers(
        reposOriginal: List<Repo>,
        reposFiltered: List<Repo>,
        mgMarkerOptions: MgMarkerOptions,
        targetReposByArgs: List<String>?,
        conf: MainConfigurations,
    ): Int {
        if (mgMarkerOptions.isEmpty()) {
            // TODO: 適切な Massgit 例外に置き換え
            error("'mg-marker edit' requires at least one option")
        }

        val targetRepos = when {
            targetReposByArgs == null -> reposFiltered.map { it.dirname }
            else -> {
                val reposFilteredNameSet = reposFiltered.map { it.dirname }.toSet()
                targetReposByArgs.filter { it in reposFilteredNameSet }
            }
        }.toSet()

        val queries = mgMarkerOptions.getEditQueries()

        runCatching {
            val editor = ReposEditor(reposOriginal, conf)
            editor.editMarkers(queries, targetRepos)
            editor.overwrite()
        }.onFailure { e ->
            throw UpdateReposFileFailedException(e)
        }
        logger.info("Repos has been updated.")
        return 0
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(MgMarkerExecutor::class.java) }
        private const val EXIT_CODE_NON_EXISTS_REPO_NAME_SPECIFIED = 1
    }
}

private enum class MgMarkerMode {
    LIST,
    EDIT,
}

private enum class MgMarkerOptionsDef(
    val names: List<String>,
    val argNum: Int,
) : jp.unaguna.massgit.common.args.OptionDef {
    Add(listOf("--add", "-a"), 1),
    Remove(listOf("--remove", "-r"), 1),
    ;
    override val representativeName = names[0]

    /**
     * Judge sufficiency of args
     *
     * @return true if the specified number is valid as the number of arguments or exceeds it, or false otherwise.
     */
    override fun sufficient(actualNum: Int): Boolean {
        return actualNum >= this.argNum
    }
}

private class MgMarkerOptions(
    private val options: Options<MgMarkerOptionsDef>,
) : Map<MgMarkerOptionsDef, List<Option<MgMarkerOptionsDef>>> by options {
    fun getEditOptions(): List<Option<MgMarkerOptionsDef>> = options.ofOrdered(
        MgMarkerOptionsDef.Add,
        MgMarkerOptionsDef.Remove,
    )

    fun getEditQueries(): List<MarkerEditQuery> {
        return this.getEditOptions().map { option ->
            when (option.def) {
                MgMarkerOptionsDef.Add -> MarkerEditQuery.Add(option.getOneArg())
                MgMarkerOptionsDef.Remove -> MarkerEditQuery.Remove(option.getOneArg())
            }
        }
    }

    override fun toString(): String {
        return options.toString()
    }

    companion object {
        fun build(args: List<String>): Pair<MgMarkerOptions, List<String>> {
            val (mainOptionsInner, remainingArgs) = Options.build(args, MgMarkerOptionsProvider)
            return Pair(MgMarkerOptions(mainOptionsInner), remainingArgs)
        }

        private object MgMarkerOptionsProvider : OptionDefProvider<MgMarkerOptionsDef> {
            private val mgMarkerOptionDef: Map<String, MgMarkerOptionsDef> = MgMarkerOptionsDef.entries
                .flatMap { it.names.map { name -> Pair(name, it) } }
                .associate { it }

            override fun getOptionDef(name: String): MgMarkerOptionsDef {
                return mgMarkerOptionDef.getOrElse(name) {
                    throw UnknownOptionException(name)
                }
            }
        }
    }
}

private class UnknownOptionException(unknownOption: String) : MassgitException("Unknown option: $unknownOption")

private class MgMarkerNullModeException : MassgitException("mode must be 'list' or 'edit'; specified mode: null")

private class IllegalMgMarkerModeException(mode: String, cause: Throwable? = null) :
    MassgitException("mode must be 'list' or 'edit'; specified mode: '$mode'", cause)

private class UpdateReposFileFailedException(cause: Throwable? = null) :
    MassgitException("failed to update repo files", cause)
