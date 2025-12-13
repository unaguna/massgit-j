package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import java.nio.file.Path
import kotlin.io.path.Path

class MainConfigurations(
    options: MainArgs.MassgitOptions,
    private val prop: Prop = Prop(),
) {
    val massProjectDir: Path
        get() = System.getProperty("jp.unaguna.massgit.projectDir")?.let { Path(it) }
            ?: Path("").toAbsolutePath()

    val reposFilePath: Path
        get() = massProjectDir.resolve(".massgit").resolve("repos.json")

    val repSuffix: String? = options.getRepSuffix()

    fun prohibitSubcommand(subcommand: String): Boolean {
        return prop.getBoolean(Prop.Key.ProhibitedSubcommands(subcommand))
    }
}
