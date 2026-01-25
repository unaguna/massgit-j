package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.common.args.Option
import jp.unaguna.massgit.common.args.OptionDefProvider
import jp.unaguna.massgit.common.args.Options
import jp.unaguna.massgit.common.files.isDirectoryContainsEntry
import jp.unaguna.massgit.common.files.toSlashedString
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposEditor
import jp.unaguna.massgit.exception.UnknownOptionException
import jp.unaguna.massgit.help.HelpDefinition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
import kotlin.streams.asSequence

class MgInitExecutor(
    private val conf: MainConfigurations,
    // private val processExecutor: ProcessExecutor = ProcessExecutor.default(),
) : SubcommandExecutor {
    override val subcommand: Subcommand = Subcommand.MgInit

    @Suppress("ReturnCount")
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        if (mainArgs.subOptions.contains("--help")) {
            printHelp()
            return 0
        }

        val (mgInitOptions, otherArgs) = MgInitOptions.build(mainArgs.subOptions)
        check(otherArgs.isEmpty())

        check(conf.massProjectDir.isDirectory()) { "massProjectDir must exist and be a directory" }

        if (conf.massProjectConfDir.isDirectoryContainsEntry()) {
            System.err.println("there is already .massgit directory: ${conf.massProjectConfDir}")
            @Suppress("MagicNumber")
            return 2
        }

        // searchDepth is depth + 1; since user specify 'depth' to search repository directory,
        // actual program searches .git under the repository directories.
        val searchDepth = mgInitOptions.depth?.let { it + 1 } ?: MINIMUM_SEARCH_DEPTH

        val gitRepoPathList = lookForGitRepos(maxDepth = searchDepth)
        val repos = gitRepoPathList.map { repoPath ->
            Repo(
                dirname = conf.massProjectDir.relativize(repoPath).toSlashedString(),
            )
        }.toList()

        // Create .massgit directory
        // Use `createDirectories` instead of `createDirectory`
        // to ensure it works even when the .massgit hierarchy is deeply customized.
        // Since the existence of `massProjectDir` is verified in previous,
        // there is no risk of accidentally creating a directory outside the project directory.
        conf.massProjectConfDir.createDirectories()

        printFoundRepoNum(repos.size)
        repos.forEach { repo ->
            println("${repo.dirname}: ${conf.massProjectDir.resolve(repo.dirname).normalize()}")
        }

        ReposEditor(repos, conf).overwrite()
        // TODO: 各 Repo の url を git remote で取得して埋める

        return 0
    }

    private fun lookForGitRepos(root: Path = conf.massProjectDir, maxDepth: Int): Sequence<Path> {
        return Files.walk(root, maxDepth)
            .asSequence()
            .filter { it.isDirectory() && it.fileName.toString() == ".git" }
            .map {
                logger.trace("Found git directory: {}", it)
                it.parent
            }
            .filter { it != root }
    }

    private fun printFoundRepoNum(num: Int) {
        when (num) {
            0 -> System.err.println("No repositories are found")
            1 -> System.err.println("1 repository is found")
            else -> System.err.println("$num repositories are found")
        }
    }

    private fun printHelp() {
        val helpDef = HelpDefinition.load()
        helpDef.printSubcommand(System.out, "massgit", "mg-init")
    }

    companion object {
        private const val MINIMUM_SEARCH_DEPTH = 2
        private val logger: Logger by lazy { LoggerFactory.getLogger(MgInitExecutor::class.java) }
    }
}

private enum class MgInitOptionsDef(
    val names: List<String>,
    val argNum: Int,
) : jp.unaguna.massgit.common.args.OptionDef {
    Depth(listOf("--depth"), 1),
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

private class MgInitOptions(
    private val options: Options<MgInitOptionsDef>,
) : Map<MgInitOptionsDef, List<Option<MgInitOptionsDef>>> by options {
    val depth: Int? = options.getOneOrNull(MgInitOptionsDef.Depth)?.getOneArg()?.toInt()

    override fun toString(): String {
        return options.toString()
    }

    companion object {
        fun build(args: List<String>): Pair<MgInitOptions, List<String>> {
            val (mainOptionsInner, remainingArgs) = Options.build(args, MgInitOptionsProvider)
            return Pair(MgInitOptions(mainOptionsInner), remainingArgs)
        }

        private object MgInitOptionsProvider : OptionDefProvider<MgInitOptionsDef> {
            private val mgInitOptionDef: Map<String, MgInitOptionsDef> = MgInitOptionsDef.entries
                .flatMap { it.names.map { name -> Pair(name, it) } }
                .associate { it }

            override fun getOptionDef(name: String): MgInitOptionsDef {
                return mgInitOptionDef.getOrElse(name) {
                    throw UnknownOptionException(name)
                }
            }
        }
    }
}
