package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.exception.MassgitException
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
            return try {
                Json.decodeFromString<List<Repo>>(reposFilePath.readText(Charsets.UTF_8))
            } catch (e: NoSuchFileException) {
                throw NoReposFileFoundException(reposFilePath, e)
            } catch (e: java.nio.file.NoSuchFileException) {
                throw NoReposFileFoundException(reposFilePath, e)
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val reposFilePath = Path(args[0])
            val repos = loadFromFile(reposFilePath)
            println(repos)
        }
    }
}

private class NoReposFileFoundException(reposPath: Path, cause: Throwable?) :
    MassgitException("the repos file is not found: $reposPath", cause)
