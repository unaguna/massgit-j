package jp.unaguna.massgit.configfile

import jp.unaguna.massgit.MainConfigurations
import jp.unaguna.massgit.exception.MassgitException

class ReposLoader(
    private val reposInj: List<Repo>? = null,
) {
    @Suppress("ThrowsCount")
    fun load(conf: MainConfigurations): List<Repo> {
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

        return reposFiltered
    }
}

private class LoadingReposFailedException(cause: Throwable) :
    MassgitException("failed to load repos file", cause)

private class NoRepositoriesRegisteredException :
    MassgitException("No target repositories have been registered.")

private class NoRepositoriesTargetedException :
    MassgitException("No repositories were targeted.")
