package jp.unaguna.massgit

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class MainArgsTest {
    @ParameterizedTest
    @MethodSource("paramsOfTestSubCommand")
    fun testRepSuffix(argsStr: List<String>, expectedSubCommand: String?) {
        val args = MainArgs.of(argsStr)

        assertEquals(expectedSubCommand, args.subCommand)
    }

    companion object {
        @JvmStatic
        fun paramsOfTestSubCommand(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), null),
            arguments(listOf("--rep-suffix", "@"), null),
            arguments(listOf("--rep-suffix=@"), null),
            arguments(listOf("--version"), null),
            arguments(listOf("grep"), "grep"),
            arguments(listOf("--rep-suffix", "@", "grep"), "grep"),
            arguments(listOf("--rep-suffix=@", "grep"), "grep"),
            arguments(listOf("grep", "--dummy"), "grep"),
            arguments(listOf("--rep-suffix", "@", "grep", "--dummy"), "grep"),
            arguments(listOf("--rep-suffix=@", "grep", "--dummy"), "grep"),
        )
    }
}
