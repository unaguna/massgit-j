package jp.unaguna.massgit

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RepSuffixTest {
    @ParameterizedTest
    @MethodSource("paramsOfTestRepSuffix")
    fun testRepSuffix(argsStr: List<String>, expectedRepSuffix: String) {
        val args = MainArgs.of(argsStr)
        val conf = MainConfigurations(args.mainOptions)
        val gitProcessManager = args.subCommand!!.gitProcessManager(args, conf)

        assertIs<GitProcessRegularManager>(gitProcessManager)
        val actualRepSuffix = gitProcessManager.repSuffix
        assertEquals(expectedRepSuffix, actualRepSuffix)
    }

    companion object {
        @JvmStatic
        fun paramsOfTestRepSuffix(): Stream<Arguments> = Stream.of(
            arguments(listOf("switch"), ": "),
            arguments(listOf("diff"), ": "),
            arguments(listOf("diff", "--name-only"), "/"),
            arguments(listOf("grep"), "/"),
            arguments(listOf("ls-files"), "/"),
        )
    }
}
