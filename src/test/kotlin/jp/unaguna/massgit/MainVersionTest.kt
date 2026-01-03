package jp.unaguna.massgit

import jp.unaguna.massgit.testcommon.process.PreErrorProcessExecutor
import jp.unaguna.massgit.testcommon.stdio.trapStdout
import kotlin.test.Test
import kotlin.test.assertEquals

class MainVersionTest {
    @Test
    fun testVersion() {
        val mainArgs = MainArgs.of(listOf("--version"))

        val actualStdout = trapStdout {
            Main().run(mainArgs, reposInj = emptyList(), processExecutor = PreErrorProcessExecutor())
        }
        println(actualStdout)

        assert(actualStdout.startsWith("massgit on java"))
        assertEquals(1, actualStdout.count { it == '\n' })
        assert(actualStdout.endsWith("\n"))
    }
}
