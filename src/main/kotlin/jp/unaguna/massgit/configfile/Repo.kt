package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.exception.MassgitException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        private val logger: Logger by lazy { LoggerFactory.getLogger(Repo::class.java) }

        fun loadFromFile(reposFilePath: Path): List<Repo> {
            return try {
                Json.decodeFromString<List<Repo>>(reposFilePath.readText(Charsets.UTF_8))
            } catch (e: NoSuchFileException) {
                throw NoReposFileFoundException(reposFilePath, e)
            } catch (e: java.nio.file.NoSuchFileException) {
                throw NoReposFileFoundException(reposFilePath, e)
            }
                .also {
                    logger.info("Repos loaded successfully: {}", reposFilePath)
                    logger.debug("Loaded repos: {}", it)
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
