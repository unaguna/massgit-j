package jp.unaguna.massgit

import java.nio.file.Path
import kotlin.io.path.Path

class MainConfigurations(
    private val options: MainArgs.Options,
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
}
