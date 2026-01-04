package jp.unaguna.massgit

import jp.unaguna.massgit.testcommon.assertion.assertNotContains
import jp.unaguna.massgit.testcommon.process.PreErrorProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutStderrResult
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class HelpTest {
    @Test
    fun `test --help`(@TempDir tempDir: Path) {
        val reposPath = tempDir.resolve("repos.json")
        val mainArgs = MainArgs.of(listOf("--help"))
        val conf = MainConfigurations(mainArgs.mainOptions, reposFilePathInj = reposPath)
        val processExecutor = PreErrorProcessExecutor()

        val expectedStderr = ""
        val expectedExitCode = 0

        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                conf,
                processExecutor = processExecutor,
            )
        }

        println(actualStdout)
        assert(actualStdout.startsWith("massgit"))
        assertContains(actualStdout, "massgit [<options>] <subcommand> [<args>]")
        assertEquals(expectedStderr, actualStderr)
        assertEquals(expectedExitCode, actualExitCode)
    }

    @Test
    fun `test mg-clone --help`(@TempDir tempDir: Path) {
        val reposPath = tempDir.resolve("repos.json")
        val mainArgs = MainArgs.of(listOf("mg-clone", "--help"))
        val conf = MainConfigurations(mainArgs.mainOptions, reposFilePathInj = reposPath)
        val processExecutor = PreErrorProcessExecutor()

        val expectedStderr = ""
        val expectedExitCode = 0

        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                conf,
                processExecutor = processExecutor,
            )
        }

        println(actualStdout)
        assertContains(actualStdout, "massgit [<root-options>] mg-clone")
        assertEquals(expectedStderr, actualStderr)
        assertEquals(expectedExitCode, actualExitCode)
    }

    @Test
    fun `test mg-marker --help`(@TempDir tempDir: Path) {
        val reposPath = tempDir.resolve("repos.json")
        val mainArgs = MainArgs.of(listOf("mg-marker", "--help"))
        val conf = MainConfigurations(mainArgs.mainOptions, reposFilePathInj = reposPath)
        val processExecutor = PreErrorProcessExecutor()

        val expectedStderr = ""
        val expectedExitCode = 0

        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                conf,
                processExecutor = processExecutor,
            )
        }

        println(actualStdout)
        assertContains(actualStdout, "massgit [<root-options>] mg-marker list [<repo>...]")
        assertContains(actualStdout, "massgit [<root-options>] mg-marker edit <options>[...] [<repo>...]")
        assertEquals(expectedStderr, actualStderr)
        assertEquals(expectedExitCode, actualExitCode)
    }

    @Test
    fun `test mg-marker list --help`(@TempDir tempDir: Path) {
        val reposPath = tempDir.resolve("repos.json")
        val mainArgs = MainArgs.of(listOf("mg-marker", "list", "--help"))
        val conf = MainConfigurations(mainArgs.mainOptions, reposFilePathInj = reposPath)
        val processExecutor = PreErrorProcessExecutor()

        val expectedStderr = ""
        val expectedExitCode = 0

        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                conf,
                processExecutor = processExecutor,
            )
        }

        println(actualStdout)
        assertContains(actualStdout, "massgit [<root-options>] mg-marker list [<repo>...]")
        assertNotContains(actualStdout, "massgit [<root-options>] mg-marker edit <options>[...] [<repo>...]")
        assertEquals(expectedStderr, actualStderr)
        assertEquals(expectedExitCode, actualExitCode)
    }

    @Test
    fun `test mg-marker edit --help`(@TempDir tempDir: Path) {
        val reposPath = tempDir.resolve("repos.json")
        val mainArgs = MainArgs.of(listOf("mg-marker", "edit", "--help"))
        val conf = MainConfigurations(mainArgs.mainOptions, reposFilePathInj = reposPath)
        val processExecutor = PreErrorProcessExecutor()

        val expectedStderr = ""
        val expectedExitCode = 0

        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                conf,
                processExecutor = processExecutor,
            )
        }

        println(actualStdout)
        assertNotContains(actualStdout, "massgit [<root-options>] mg-marker list [<repo>...]")
        assertContains(actualStdout, "massgit [<root-options>] mg-marker edit <options>[...] [<repo>...]")
        assertContains(actualStdout, "--add")
        assertContains(actualStdout, "--remove")
        assertEquals(expectedStderr, actualStderr)
        assertEquals(expectedExitCode, actualExitCode)
    }
}
