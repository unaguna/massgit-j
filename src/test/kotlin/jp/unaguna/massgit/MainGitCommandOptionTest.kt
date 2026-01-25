package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Prop
import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.bufferedWriter
import kotlin.io.path.writer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MainGitCommandOptionTest {
    @ParameterizedTest
    @MethodSource("params of test git config -c")
    fun `test git config -c`(configOptions: Array<String>) {
        // Prerequisites for simplifying assertions
        check(configOptions.filterIndexed { i, _ -> i % 2 == 0 }.toSet() == setOf("-c"))

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
            MainArgs.of(listOf(*configOptions, "ls-files")),
            reposInj = repos,
            processExecutor = processExecutor,
        )

        assertEquals(repos.size, processExecutor.executeCount)
        // assert git command is 'git ** [-c <config>]... ** ls-files'
        for (history in processExecutor.getHistories()) {
            assertEquals("git", history.command.firstOrNull())
            assertEquals("ls-files", history.command.lastOrNull())
            assertEquals(configOptions.count { it == "-c" }, history.command.count { it == "-c" })
            val indexOfC = history.command.indexOf("-c")
                .also { check(it >= 0) }
            assertContentEquals(
                configOptions.toList(),
                history.command.subList(indexOfC, indexOfC + configOptions.size),
            )
        }
    }

    @Test
    fun `test git config -c with props`(@TempDir tempDir: Path) {
        val configOptions = arrayOf("-c", "example.conf1=1", "-c", "example.conf2=2")

        // Prerequisites for simplifying assertions
        check(configOptions.filterIndexed { i, _ -> i % 2 == 0 }.toSet() == setOf("-c"))

        val repos = listOf(
            Repo(dirname = "repo1", markers = listOf("m1", "m2")),
            Repo(dirname = "repo2", markers = listOf("m1")),
            Repo(dirname = "repo3", markers = listOf("m2")),
            Repo(dirname = "repo4"),
        )
        val systemPropPath = tempDir.resolve("config.properties")
            .also { systemPropPath ->
                systemPropPath.bufferedWriter().use { writer ->
                    writer.write("git.config.example.conf1=overwritten")
                    writer.newLine()
                    writer.write("git.config.example.conf3=3")
                    writer.newLine()
                }
            }
        val processExecutor = DummyProcessExecutor(
            exitCodes = listOf(0, 0, 0, 0),
        )
        val expectedArgs = listOf("-c", "example.conf1=1", "-c", "example.conf2=2", "-c", "example.conf3=3")

        val prop = Prop(
            systemUrl = systemPropPath.toUri().toURL(),
        )
        val mainArgs = MainArgs.of(listOf(*configOptions, "ls-files"))

        Main().run(
            mainArgs,
            MainConfigurations(
                mainArgs.mainOptions,
                prop,
            ),
            reposInj = repos,
            processExecutor = processExecutor,
        )

        assertEquals(repos.size, processExecutor.executeCount)
        // assert git command is 'git ** [-c <config>]... ** ls-files'
        for (history in processExecutor.getHistories()) {
            assertEquals("git", history.command.firstOrNull())
            assertEquals("ls-files", history.command.lastOrNull())
            assertEquals(expectedArgs.count { it == "-c" }, history.command.count { it == "-c" })
            val indexOfC = history.command.indexOf("-c")
                .also { check(it >= 0) }
            assertContentEquals(
                expectedArgs,
                history.command.subList(indexOfC, indexOfC + expectedArgs.size),
            )
        }
    }

    companion object {
        @JvmStatic
        fun `params of test git config -c`(): Stream<Arguments> = Stream.of(
            arguments(
                arrayOf("-c", "core.autocrlf=true"),
            ),
            arguments(
                arrayOf("-c", "core.autocrlf=true", "-c", "core.example=1"),
            ),
            arguments(
                arrayOf("-c", "core.autocrlf=true", "-c", "core.example=example=2"),
            ),
        )
    }
}
