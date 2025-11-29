package jp.unaguna.massgit

import java.nio.file.Path
import kotlin.io.path.Path

class MainConfigurations(
    private val mainArgs: MainArgs,
) {
    val massProjectDir: Path
        get() = System.getProperty("jp.unaguna.massgit.projectDir")?.let { Path(it) }
            ?: Path("").toAbsolutePath()

    val reposFilePath: Path
        get() = massProjectDir.resolve(".massgit").resolve("repos.json")
}
