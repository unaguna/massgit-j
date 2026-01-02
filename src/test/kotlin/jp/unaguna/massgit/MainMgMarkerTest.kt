package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.process.PreErrorProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdout
import kotlin.test.Test
import kotlin.test.assertEquals

class MainMgMarkerTest {
    @Test
    fun `test mg-marker`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedStdout = buildStringByPrintStream {
            println("repo1 m1,m2")
            println("repo2 m1")
            println("repo3 m2")
            println("repo4 ")
        }

        val actualStdout = trapStdout {
            Main().run(
                MainArgs.of(listOf("mg-marker")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
    }

    @Test
    fun `test mg-marker with marker`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedStdout = buildStringByPrintStream {
            println("repo1 m1,m2")
            println("repo3 m2")
        }

        val actualStdout = trapStdout {
            Main().run(
                MainArgs.of(listOf("-m", "m2", "mg-marker")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
    }
}
