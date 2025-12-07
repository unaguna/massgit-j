package jp.unaguna.massgit.configfile

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readText

@Serializable
data class Repo(
    val url: String? = null,
    val dirname: String,
    val markers: List<String> = emptyList(),
) {
    companion object {
        fun loadFromFile(reposFilePath: Path): List<Repo> {
            return Json.decodeFromString<List<Repo>>(reposFilePath.readText(Charsets.UTF_8))
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val reposFilePath = Path(args[0])
            val repos = loadFromFile(reposFilePath)
            println(repos)
        }
    }
}
