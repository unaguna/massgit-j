package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.exception.LoadingReposFailedException
import jp.unaguna.massgit.exception.NoRepositoriesRegisteredException
import jp.unaguna.massgit.exception.NoRepositoriesTargetedException
import org.slf4j.LoggerFactory

class MgMarkerExecutor(
    override val subcommand: Subcommand,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val repos = reposInj ?: runCatching {
            Repo.loadFromFile(conf.reposFilePath)
        }.getOrElse { t -> throw LoadingReposFailedException(t) }
        if (repos.isEmpty()) {
            throw NoRepositoriesRegisteredException()
        }
        val reposFiltered = when (val markerConditions = conf.markerConditions) {
            null -> repos
            else -> repos.filter { markerConditions.satisfies(it.markers) }
        }
        if (reposFiltered.isEmpty()) {
            throw NoRepositoriesTargetedException()
        }
        logger.debug("Repos filtered: {}", reposFiltered)

        return listMarkers(reposFiltered)
    }

    private fun listMarkers(reposFiltered: List<Repo>): Int {
        for (repo in reposFiltered) {
            println("${repo.dirname} ${repo.markers.joinToString(",")}")
        }
        return 0
    }

    companion object {
        private val logger by lazy { LoggerFactory.getLogger(MgMarkerExecutor::class.java) }
    }
}
