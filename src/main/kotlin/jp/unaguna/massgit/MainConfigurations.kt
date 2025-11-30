package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import java.nio.file.Path
import kotlin.io.path.Path

class MainConfigurations(
    private val options: MainArgs.Options,
    private val prop: Prop = Prop(),
) {
    val massProjectDir: Path
        get() = System.getProperty("jp.unaguna.massgit.projectDir")?.let { Path(it) }
            ?: Path("").toAbsolutePath()

    val reposFilePath: Path
        get() = massProjectDir.resolve(".massgit").resolve("repos.json")

    val repSuffix: String?
        get() {
            val option = options.of(MainArgs.OptionDef.REP_SUFFIX).getOrNull(0)
            if (option != null) {
                return option.args[0]
            }

            return null
        }

    fun prohibitSubcommand(subcommand: String): Boolean {
        return prop.getBoolean(Prop.Key.ProhibitedSubcommands(subcommand))
    }
}
