package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MainReposFilterTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "-m,m1,repo1:repo2",
            "-m,m2,repo1:repo3",
            "-m,m1 and m2,repo1",
            "-m,m1 or m2,repo1:repo2:repo3",
            "--marker,m1,repo1:repo2",
            "--marker,m2,repo1:repo3",
            "--marker,m1 and m2,repo1",
            "--marker,m1 or m2,repo1:repo2:repo3",
        ]
    )
    fun testSubCommand(optM: String, markerExpression: String, expectedReposStr: String) {
        val expectedRepos = expectedReposStr.split(":")
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val gitProcessManagerFactory = DummyProcessManagerFactory()

        Main().run(
            MainArgs.of(listOf(optM, markerExpression, "ls-files")),
            reposInj = repos,
            gitProcessManagerFactoryInj = gitProcessManagerFactory,
        )

        assertEquals(1, gitProcessManagerFactory.reposHistory.size)
        assertContentEquals(expectedRepos, gitProcessManagerFactory.reposHistory[0].map { it.dirname })
    }

    private class DummyProcessManagerFactory : GitProcessManagerFactory {
        val reposHistory = mutableListOf<List<Repo>>()

        override fun create(): GitProcessManager = object : GitProcessManager {
            override fun run(
                repos: List<Repo>,
                massgitBaseDir: Path?
            ): Int {
                reposHistory.add(repos)
                return 0
            }
        }
    }
}
