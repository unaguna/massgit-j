package jp.unaguna.massgit

import jp.unaguna.massgit.configfile.Repo
import jp.unaguna.massgit.testcommon.io.buildStringByPrintStream
import jp.unaguna.massgit.testcommon.process.DummyProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdoutAndResult
import jp.unaguna.massgit.testcommon.stdio.trapStdoutStderrResult
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MainMgInitTest {
    @Test
    fun `test mg-init`(@TempDir tempDir: Path) {
        val processExecutor = DummyProcessExecutor(listOf(0, 0, 0, 0))
        val workingDir = tempDir.resolve("pwd").apply {
            createDirectory()
        }
        val expectedMassgitDir = workingDir.resolve(".massgit")
        val expectedReposPath = expectedMassgitDir.resolve("repos.json")
        val expectedRepos = listOf(
            Repo(dirname = "repo1"),
            Repo(dirname = "repo2"),
        )
        val expectedStdout = buildStringByPrintStream {
            expectedRepos.forEach { repo ->
                println("${repo.dirname}: ${workingDir.resolve(repo.dirname).normalize()}")
            }
        }

        // create mock repositories
        expectedRepos.forEach { expectedRepo ->
            val repoPath = workingDir.resolve(expectedRepo.dirname).createDirectory()
            println("created directory: $repoPath")
            val gitPath = repoPath.resolve(".git").createDirectory()
            println("created directory: $gitPath")
        }
        val repoPath = workingDir.resolve("inner/repo/.git").createDirectories()
        println("created directory: $repoPath")
        val dummyRepo = workingDir.resolve("dummy").createDirectory()
        println("created directory: $dummyRepo")
        val dummyRepo2 = workingDir.resolve("dummy2").createDirectory()
        println("created directory: $dummyRepo2")
        val dummyRepo2Git = dummyRepo2.resolve(".git").createFile()
        println("created file: $dummyRepo2Git")

        val mainArgs = MainArgs.of(listOf("mg-init"))
        val conf = MainConfigurations(
            mainArgs.mainOptions,
            massProjectDirInj = workingDir,
        )
        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                mainArgs,
                confInj = conf,
                processExecutor = processExecutor,
            )
        }

        assertEquals(0, actualExitCode)
        assert(expectedMassgitDir.isDirectory())
        assert(expectedReposPath.isRegularFile())
        assertContentEquals(expectedRepos, Repo.loadFromFile(expectedReposPath))
        assertEquals(expectedStdout, actualStdout)
    }

    @Test
    fun `test mg-init with --depth`(@TempDir tempDir: Path) {
        val processExecutor = DummyProcessExecutor(listOf(0, 0, 0, 0))
        val workingDir = tempDir.resolve("pwd").apply {
            createDirectory()
        }
        val expectedMassgitDir = workingDir.resolve(".massgit")
        val expectedReposPath = expectedMassgitDir.resolve("repos.json")
        val expectedRepos = listOf(
            Repo(dirname = "inner/repo"),
            Repo(dirname = "repo1"),
            Repo(dirname = "repo2"),
        )
        val expectedStdout = buildStringByPrintStream {
            expectedRepos.forEach { repo ->
                println("${repo.dirname}: ${workingDir.resolve(repo.dirname).normalize()}")
            }
        }

        // create mock repositories
        expectedRepos.forEach { expectedRepo ->
            val repoPath = workingDir.resolve(expectedRepo.dirname).createDirectories()
            println("created directory: $repoPath")
            val gitPath = repoPath.resolve(".git").createDirectory()
            println("created directory: $gitPath")
        }
        val repoPath = workingDir.resolve("inner/inner/repo/.git").createDirectories()
        println("created directory: $repoPath")
        val dummyRepo = workingDir.resolve("dummy").createDirectory()
        println("created directory: $dummyRepo")
        val dummyRepo2 = workingDir.resolve("dummy2").createDirectory()
        println("created directory: $dummyRepo2")
        val dummyRepo2Git = dummyRepo2.resolve(".git").createFile()
        println("created file: $dummyRepo2Git")

        val mainArgs = MainArgs.of(listOf("mg-init", "--depth", "2"))
        val conf = MainConfigurations(
            mainArgs.mainOptions,
            massProjectDirInj = workingDir,
        )
        val (actualStdout, actualExitCode) = trapStdoutAndResult {
            Main().run(
                mainArgs,
                confInj = conf,
                processExecutor = processExecutor,
            )
        }

        assertEquals(0, actualExitCode)
        assert(expectedMassgitDir.isDirectory())
        assert(expectedReposPath.isRegularFile())
        assertContentEquals(expectedRepos, Repo.loadFromFile(expectedReposPath))
        assertEquals(expectedStdout, actualStdout)
    }

    @Test
    fun `Error if massgit dir is already exist`(@TempDir tempDir: Path) {
        val processExecutor = DummyProcessExecutor(listOf(0, 0, 0, 0))
        val workingDir = tempDir.resolve("pwd").apply {
            createDirectory()
        }
        val expectedMassgitDir = workingDir.resolve(".massgit")
        val expectedReposPath = expectedMassgitDir.resolve("repos.json")
        val expectedRepos = listOf(
            Repo(dirname = "repo1"),
            Repo(dirname = "repo2"),
        )

        // create mock repositories
        expectedRepos.forEach { expectedRepo ->
            val repoPath = workingDir.resolve(expectedRepo.dirname).createDirectory()
            println("created directory: $repoPath")
            val gitPath = repoPath.resolve(".git").createDirectory()
            println("created directory: $gitPath")
        }
        val dummyRepo = workingDir.resolve("dummy").createDirectory()
        println("created directory: $dummyRepo")
        val dummyRepo2 = workingDir.resolve("dummy2").createDirectory()
        println("created directory: $dummyRepo2")
        val dummyRepo2Git = dummyRepo2.resolve(".git").createFile()
        println("created file: $dummyRepo2Git")

        // create massgit directory
        expectedMassgitDir.createDirectory()
        expectedReposPath.createFile()

        val mainArgs = MainArgs.of(listOf("mg-init"))
        val conf = MainConfigurations(
            mainArgs.mainOptions,
            massProjectDirInj = workingDir,
        )
        val (actualStdout, actualStderr, actualExitCode) = trapStdoutStderrResult {
            Main().run(
                mainArgs,
                confInj = conf,
                processExecutor = processExecutor,
            )
        }

        assertEquals(2, actualExitCode)
        assertContains(actualStderr, "there is already .massgit directory")
        assertEquals("", actualStdout)
        // assert that repos.json is not updated
        assertEquals(0L, expectedReposPath.fileSize())
    }
}
