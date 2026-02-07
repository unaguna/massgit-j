package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.io.createTempTextFile
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutStderr
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GitProcessManagerPullTest {
    @Test
    fun test_stdout(@TempDir tempDir: Path) {
        val mainArgs = MainArgs.of(listOf("pull"))
        val conf = MainConfigurations(mainArgs.mainOptions)
        val exitCodes = listOf(0, 0, 0)
        val repos = List(exitCodes.size) { index ->
            Repo(dirname = "repo$index")
        }
        val eachProcessStdout = listOf(
            createTempTextFile(tempDir, "stdout") {
                println("output something")
            },
            createTempTextFile(tempDir, "stdout") {
                println("Already up to date.")
            },
            createTempTextFile(tempDir, "stdout") {
                println("output something")
            },
        )
        val expectedStdout = buildStringByPrintStream {
            println("repo0: output something")
            println("repo1: Already up to date.")
            println("repo2: output something")
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
        assertContains(actualStderr, "Success: 2, Already UP-TO-DATE: 1, Failed: 0, Total: 3")
        assertEquals(repos.size, processExecutor.executeCount)
    }
}
