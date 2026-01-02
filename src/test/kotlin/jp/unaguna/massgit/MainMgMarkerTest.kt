package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.process.PreErrorProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutAndResult
import kotlin.test.Test
import kotlin.test.assertEquals

class MainMgMarkerTest {
    @Test
    fun `test 'mg-marker list'`() {
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

        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                MainArgs.of(listOf("mg-marker", "list")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
        assertEquals(0, actualExitCode)
    }

    @Test
    fun `test 'mg-marker list' with marker`() {
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

        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                MainArgs.of(listOf("-m", "m2", "mg-marker", "list")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
        assertEquals(0, actualExitCode)
    }

    @Test
    fun `test 'mg-marker list' with args`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedStdout = buildStringByPrintStream {
            println("repo4 ")
            println("repo1 m1,m2")
        }

        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                MainArgs.of(listOf("mg-marker", "list", "repo4", "repo1")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
        assertEquals(0, actualExitCode)
    }

    @Test
    fun `test 'mg-marker list' with args and marker`() {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedStdout = buildStringByPrintStream {
            println("repo1 m1,m2")
        }

        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                MainArgs.of(listOf("-m", "m1", "mg-marker", "list", "repo4", "repo1")),
                reposInj = repos,
                processExecutor = processExecutor,
            )
        }

        assertEquals(expectedStdout, actualStdout)
        assertEquals(0, actualExitCode)
    }
}
