package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import kotlin.test.Test
import kotlin.test.assertEquals

class MainMgCloneTest {
    @Test
    fun `test mg-clone`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2"), url = "https://example.com/repo1"),
            Repo(dirname = "repo2", markers = listOf("m1"), url = "https://example.com/repo2"),
            Repo(dirname = "repo3", markers = listOf("m2"), url = "https://example.com/repo3"),
            Repo(dirname = "repo4", url = "https://example.com/repo4"),
        )
        val processExecutor = DummyProcessExecutor(listOf(0, 0, 0, 0))

        Main().run(
            MainArgs.of(listOf("mg-clone")),
            reposInj = repos,
            processExecutor = processExecutor,
        )

        assertEquals(repos.size, processExecutor.executeCount)
        repos.forEachIndexed { i, repo ->
            assertEquals(
                listOf("git", "clone", repo.url, repo.dirname),
                processExecutor.getHistory(i).command,
            )
        }
    }

    @Test
    fun `test mg-clone with marker`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2"), url = "https://example.com/repo1"),
            Repo(dirname = "repo2", markers = listOf("m1"), url = "https://example.com/repo2"),
            Repo(dirname = "repo3", markers = listOf("m2"), url = "https://example.com/repo3"),
            Repo(dirname = "repo4", url = "https://example.com/repo4"),
        )
        val processExecutor = DummyProcessExecutor(listOf(0, 0, 0, 0))

        Main().run(
            MainArgs.of(listOf("-m", "m2", "mg-clone")),
            reposInj = repos,
            processExecutor = processExecutor,
        )

        assertEquals(2, processExecutor.executeCount)
        assertEquals(
            listOf("git", "clone", "https://example.com/repo1", "repo1"),
            processExecutor.getHistory(0).command,
        )
        assertEquals(
            listOf("git", "clone", "https://example.com/repo3", "repo3"),
            processExecutor.getHistory(1).command,
        )
    }
}
