package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.test.assertEquals

class GitProcessManagerGrepTest {
    @ParameterizedTest
    @CsvSource(value = [
        "0:0:0,0",
        "1:0:0,0",
        "0:0:1,0",
        "0:1:1,0",
        // not found (1) if all processes return not found (1)
        "1:1:1,1",
        // error if one or more processes return error
        "2:1:1,2",
        "1:2:1,2",
        "1:1:2,2",
        "2:0:0,2",
        "0:2:0,2",
        "0:0:2,2",
        "0:2:5,5",
    ])
    fun test_exit_codes(
        exitCodesStr: String,
        expectedExitCode: Int,
        @TempDir tempDir: Path,
    ) {
        val mainArgs = MainArgs.of(listOf("grep", "word"))
        val exitCodes = exitCodesStr.split(":").map { it.toInt() }
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val processExecutor = DummyProcessExecutor(exitCodes)
        val processManager = GitProcessManager.regular(
            mainArgs,
            processExecutor,
        )

        val actualExitCode = processManager.run(repos, massgitBaseDir = tempDir)
        assertEquals(expectedExitCode, actualExitCode)
        assertEquals(repos.size, processExecutor.executeCount)
    }
}
