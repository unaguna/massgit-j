package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposEditor
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

    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        if (mainArgs.subOptions.contains("--help")) {
            printHelp()
            return 0
        }

        check(conf.massProjectDir.isDirectory()) { "massProjectDir must exist and be a directory" }

        // TODO: 探索の深さを指定できるようにする
        val gitRepoPathList = lookForGitRepos(maxDepth = MINIMUM_SEARCH_DEPTH)
        val repos = gitRepoPathList.map { repoPath ->
            Repo(
                dirname = conf.massProjectDir.relativize(repoPath).toString(),
            )
        }.toList()

        // Create .massgit directory
        // Use `createDirectories` instead of `createDirectory`
        // to ensure it works even when the .massgit hierarchy is deeply customized.
        // Since the existence of `massProjectDir` is verified in previous,
        // there is no risk of accidentally creating a directory outside the project directory.
        conf.massProjectConfDir.createDirectories()

        ReposEditor(repos, conf).overwrite()
        // TODO: 各 Repo の url を git remote で取得して埋める

        return 0
    }

    private fun lookForGitRepos(root: Path = conf.massProjectDir, maxDepth: Int): Sequence<Path> {
        // TODO: .git が見つかったらその中を無視するようにすることで、高速化を図る

        return Files.walk(root, maxDepth)
            .asSequence()
            .filter { it.isDirectory() && it.fileName.toString() == ".git" }
            .map {
                logger.trace("Found git directory: {}", it)
                it.parent
            }
            .filter { it != root }
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
