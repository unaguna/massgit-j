package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.MainConfigurations
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.writeText

class ReposWriter {
    fun overwrite(repos: List<Repo>, conf: MainConfigurations) {
        val path = conf.reposFilePath

        val jsonStr = Json.encodeToString(repos)

        logger.debug("Writing new repos to {}", path)
        logger.trace("new repos: {}", repos)
        path.writeText(jsonStr)
    }

    companion object {
        private val logger: Logger by lazy { LoggerFactory.getLogger(ReposWriter::class.java) }
    }
}
