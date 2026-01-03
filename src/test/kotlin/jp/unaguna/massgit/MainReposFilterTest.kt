package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertContentEquals

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
        val processExecutor = DummyProcessExecutor(
            exitCodes = listOf(0, 0, 0, 0),
        )

        Main().run(
            MainArgs.of(listOf(optM, markerExpression, "ls-files")),
            reposInj = repos,
            processExecutor = processExecutor,
        )

        assertContentEquals(expectedRepos, processExecutor.getHistories().map { it.dirname })
    }
}
