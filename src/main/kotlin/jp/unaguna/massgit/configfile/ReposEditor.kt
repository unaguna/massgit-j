package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.MainConfigurations
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.writeText

class ReposEditor(
    repos: List<Repo>,
    private val conf: MainConfigurations,
) {
    private val currentRepos = repos.toMutableList()

    fun editMarkers(queries: List<MarkerEditQuery>, targetRepos: Set<String>) {
        synchronized(currentRepos) {
            currentRepos.forEachIndexed { i, repo ->
                if (targetRepos.contains(repo.dirname)) {
                    val newMarkers = repo.markers.toMutableList()
                    queries.forEach { query -> query.edit(newMarkers) }
                    currentRepos[i] = repo.copy(markers = newMarkers)
                }
            }
        }
    }

    fun overwrite() {
        val path = conf.reposFilePath

        logger.debug("Writing new repos to {}", path)
        val jsonStr = synchronized(currentRepos) {
            Json.encodeToString(currentRepos)
        }

        logger.trace("new repos: {}", jsonStr)
        path.writeText(jsonStr)
    }

    companion object {
        private val logger: Logger by lazy { LoggerFactory.getLogger(ReposEditor::class.java) }
    }
}

sealed class MarkerEditQuery {
    abstract fun edit(markers: MutableList<String>)

    class Add(val marker: String) : MarkerEditQuery() {
        override fun edit(markers: MutableList<String>) {
            if (!markers.contains(marker)) {
                markers.add(marker)
            }
        }
    }

    class Remove(val marker: String) : MarkerEditQuery() {
        override fun edit(markers: MutableList<String>) {
            markers.remove(marker)
        }
    }
}
