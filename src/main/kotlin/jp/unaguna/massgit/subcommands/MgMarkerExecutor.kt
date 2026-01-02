package jp.unaguna.massgit.subcommands

import jp.unaguna.massgit.MainArgs
import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.Subcommand
import jp.unaguna.massgit.SubcommandExecutor
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.configfile.ReposLoader
import org.slf4j.LoggerFactory

class MgMarkerExecutor(
    override val subcommand: Subcommand,
    private val reposInj: List<Repo>? = null,
) : SubcommandExecutor {
    override fun execute(conf: MainConfigurations, mainArgs: MainArgs): Int {
        val reposFiltered = ReposLoader(reposInj).load(conf)
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
