package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStderr
import jp.unaguna.massgit.testcommon.stdio.trapStdoutStderr
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.text.Regex

class GitProcessManagerFetchTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "0:0:0,0,3,0",
            // error if one or more processes return error
            "1:0:0,1,2,1",
            "0:0:1,1,2,1",
            "0:1:1,1,1,2",
            "1:1:1,1,0,3",
            "2:1:1,2,0,3",
            "1:2:1,2,0,3",
            "1:1:2,2,0,3",
            "2:0:0,2,2,1",
            "0:2:0,2,2,1",
            "0:0:2,2,2,1",
        ]
    )
    fun test_exit_codes(
        exitCodesStr: String,
        expectedExitCode: Int,
        expectedSuccessCnt: Int,
        expectedFailedCnt: Int,
        @TempDir tempDir: Path,
    ) {
        val expectedTotalCnt = expectedSuccessCnt + expectedFailedCnt
        val mainArgs = MainArgs.of(listOf("fetch"))
        val exitCodes = exitCodesStr.split(":").map { it.toInt() }
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val processExecutor = DummyProcessExecutor(exitCodes)
        val processManager = GitProcessManager.regular(
            mainArgs,
            processExecutor,
        )

        var actualExitCode: Int? = null
        val actualStderr = trapStderr {
            actualExitCode = processManager.run(repos, massgitBaseDir = tempDir)
        }
        assertEquals(expectedExitCode, actualExitCode!!)
        assertContains(
            actualStderr,
            "Success: $expectedSuccessCnt, Failed: $expectedFailedCnt, Total: $expectedTotalCnt",
        )
        assertEquals(repos.size, processExecutor.executeCount)
    }

    @Test
    fun test_empty_stdout(
        @TempDir tempDir: Path,
    ) {
        val mainArgs = MainArgs.of(listOf("fetch"))
        val exitCodes = listOf(0, 0, 0)
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val eachProcessStdout = tempDir.resolve("empty").createFile()
            .let { emptyPath -> List(exitCodes.size) { emptyPath } }
        val expectedStdout = ""

        val processExecutor = DummyProcessExecutor(exitCodes, stdout = eachProcessStdout)
        val processManager = GitProcessManager.regular(
            mainArgs,
            processExecutor,
        )

        val (actualStdout, actualStderr) = trapStdoutStderr {
            processManager.run(repos, massgitBaseDir = tempDir)
        }
        assertEquals(expectedStdout, actualStdout)
        assertContains(actualStderr, Regex("Success: \\d+, Failed: \\d+, Total: \\d+"))
        assertEquals(repos.size, processExecutor.executeCount)
    }
}
