package jp.unaguna.massgit

import jp.unaguna.massgit.testcommon.stdio.trapStdout
import kotlin.test.Test

class MainVersionTest {
    @Test
    fun testVersion() {
        val mainArgs = MainArgs.of(listOf("--version"))

        val actualStdout = trapStdout {
            Main().run(mainArgs, reposInj = emptyList(), gitProcessManagerFactoryInj = DummyProcessManagerFactory)
        }

        assert(actualStdout.startsWith("massgit on java"))
        assert(actualStdout.count { it == '\n' } == 1)
        assert(actualStdout.endsWith("\n"))
    }

    private object DummyProcessManagerFactory : GitProcessManagerFactory {
        override fun create(): GitProcessManager {
            error("must not be called")
        }
    }
}
