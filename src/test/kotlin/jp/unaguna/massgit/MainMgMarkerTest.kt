package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.process.PreErrorProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutAndResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContentEquals
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

    @ParameterizedTest
    @MethodSource("params of test 'mg-marker edit")
    fun `test 'mg-marker edit'`(
        mainOptions: Array<String>,
        editOptions: Array<String>,
        expectedNewRepos: List<Repo>,
        @TempDir tempDir: Path,
    ) {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val reposPath = tempDir.resolve("repos.json").apply {
            writeText(Json.encodeToString(repos))
        }
        val mainArgs = MainArgs.of(listOf(*mainOptions, "mg-marker", "edit", *editOptions))
        val conf = MainConfigurations(
            mainArgs.mainOptions,
            reposFilePathInj = reposPath,
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedStdout = ""

        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                mainArgs,
                confInj = conf,
                processExecutor = processExecutor,
            )
        }

        val actualNewRepos = Repo.loadFromFile(reposPath)

        assertContentEquals(expectedNewRepos, actualNewRepos)
        assertEquals(expectedStdout, actualStdout)
        assertEquals(0, actualExitCode)
    }

    @Test
    fun `If 'mg-marker edit --add' twice, the marker added only once, not duplicated`(@TempDir tempDir: Path) {
        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val reposPath = tempDir.resolve("repos.json").apply {
            writeText(Json.encodeToString(repos))
        }
        val mainArgs = MainArgs.of(listOf("mg-marker", "edit", "--add", "sample"))
        val conf = MainConfigurations(
            mainArgs.mainOptions,
            reposFilePathInj = reposPath,
        )
        val processExecutor = PreErrorProcessExecutor()
        val expectedNewRepos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2", "sample")),
            Repo(dirname = "repo2", markers = listOf("m1", "sample")),
            Repo(dirname = "repo3", markers = listOf("m2", "sample")),
            Repo(dirname = "repo4", markers = listOf("sample")),
        )

        // run twice
        repeat(2) {
            Main().run(
                mainArgs,
                confInj = conf,
                processExecutor = processExecutor,
            )
        }

        val actualNewRepos = Repo.loadFromFile(reposPath)

        assertContentEquals(expectedNewRepos, actualNewRepos)
    }

    companion object {
        @JvmStatic
        @Suppress("LongMethod")
        fun `params of test 'mg-marker edit`(): Stream<Arguments> = Stream.of(
            // add to all repos
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m1", "m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1", "new")),
                    Repo(dirname = "repo3", markers = listOf("m2", "new")),
                    Repo(dirname = "repo4", markers = listOf("new")),
                )
            ),
            // remove from all repos
            arguments(
                emptyArray<String>(),
                arrayOf("--remove", "m1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2")),
                    Repo(dirname = "repo2", markers = emptyList()),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add and remove to/from all repos
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new", "--remove", "m1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("new")),
                    Repo(dirname = "repo3", markers = listOf("m2", "new")),
                    Repo(dirname = "repo4", markers = listOf("new")),
                )
            ),
            // add to marker targeted repos
            arguments(
                arrayOf("-m", "m2"),
                arrayOf("--add", "new"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m1", "m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2", "new")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // remove from marker targeted repos
            arguments(
                arrayOf("-m", "m2"),
                arrayOf("--remove", "m1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add and remove to/from marker targeted repos
            arguments(
                arrayOf("-m", "m2"),
                arrayOf("--add", "new", "--remove", "m1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2", "new")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add to args targeted 1 repo
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new", "repo1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m1", "m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add to args targeted repos
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new", "repo1", "repo2"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m1", "m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1", "new")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // remove from args targeted 1 repo
            arguments(
                emptyArray<String>(),
                arrayOf("--remove", "m1", "repo1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // remove from args targeted repos
            arguments(
                emptyArray<String>(),
                arrayOf("--remove", "m1", "repo1", "repo2"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2")),
                    Repo(dirname = "repo2", markers = emptyList()),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add and remove to/from targeted 1 repo
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new", "--remove", "m1", "repo1"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // add and remove to/from targeted repos
            arguments(
                emptyArray<String>(),
                arrayOf("--add", "new", "--remove", "m1", "repo1", "repo3"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2", "new")),
                    Repo(dirname = "repo4", markers = emptyList()),
                )
            ),
            // specify target with both -m and arg
            arguments(
                arrayOf("-m", "m2"),
                arrayOf("--add", "new", "--remove", "m1", "repo1", "repo2"),
                listOf(
                    Repo(dirname = "repo1", markers = listOf("m2", "new")),
                    Repo(dirname = "repo2", markers = listOf("m1")),
                    Repo(dirname = "repo3", markers = listOf("m2")),
                    Repo(dirname = "repo4"),
                )
            ),
        )
    }
}
