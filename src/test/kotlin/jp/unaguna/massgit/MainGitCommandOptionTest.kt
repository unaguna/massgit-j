package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
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

    companion object {
        @JvmStatic
        fun `params of test git config -c`(): Stream<Arguments> = Stream.of(
            arguments(
                arrayOf("-c", "core.autocrlf=true"),
            ),
            arguments(
                arrayOf("-c", "core.autocrlf=true", "-c", "core.example=1"),
            ),
        )
    }
}
