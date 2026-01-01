package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.assertion.assertNotContains
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.io.createTempTextFile
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutStderr
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class GitProcessManagerLsFilesTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "0:0:0,0",
            // error if one or more processes return error
            "1:0:0,1",
            "0:0:1,1",
            "0:1:1,1",
            "1:1:1,1",
            "2:1:1,2",
            "1:2:1,2",
            "1:1:2,2",
            "2:0:0,2",
            "0:2:0,2",
            "0:0:2,2",
        ]
    )
    fun test_exit_codes(
        exitCodesStr: String,
        expectedExitCode: Int,
        @TempDir tempDir: Path,
    ) {
        val mainArgs = MainArgs.of(listOf("ls-files"))
        val conf = MainConfigurations(mainArgs.mainOptions)
        val exitCodes = exitCodesStr.split(":").map { it.toInt() }
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val processExecutor = DummyProcessExecutor(exitCodes)
        val processManager = mainArgs.subCommand!!.gitProcessManager(
            mainArgs,
            conf,
            processExecutor,
        )

        val actualExitCode = processManager.run(repos, massgitBaseDir = tempDir)
        assertEquals(expectedExitCode, actualExitCode)
        assertEquals(repos.size, processExecutor.executeCount)
    }

    @Test
    fun test_stdout(
        @TempDir tempDir: Path,
    ) {
        val mainArgs = MainArgs.of(listOf("ls-files"))
        val conf = MainConfigurations(mainArgs.mainOptions)
        val exitCodes = listOf(0, 0, 0)
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val eachProcessStdout = List(exitCodes.size) { index ->
            createTempTextFile(tempDir, "stdout$index") {
                println("gradle.properties")
                println("src/main/resources/file$index")
            }
        }
        val expectedStdout = buildStringByPrintStream {
            println("repo0/gradle.properties")
            println("repo0/src/main/resources/file0")
            println("repo1/gradle.properties")
            println("repo1/src/main/resources/file1")
            println("repo2/gradle.properties")
            println("repo2/src/main/resources/file2")
        }

        val processExecutor = DummyProcessExecutor(exitCodes, stdout = eachProcessStdout)
        val processManager = mainArgs.subCommand!!.gitProcessManager(
            mainArgs,
            conf,
            processExecutor,
        )

        val (actualStdout, actualStderr) = trapStdoutStderr {
            processManager.run(repos, massgitBaseDir = tempDir)
        }
        assertEquals(expectedStdout, actualStdout)
        // not contain summary in stderr
        assertNotContains(actualStderr, "Total")
        assertEquals(repos.size, processExecutor.executeCount)
    }
}
