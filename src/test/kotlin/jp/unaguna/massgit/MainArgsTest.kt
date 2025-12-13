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
    fun testSubCommand(argsStr: List<String>, expectedSubCommand: String?) {
        val args = MainArgs.of(argsStr)

        assertEquals(expectedSubCommand, args.subCommand)
    }

    @ParameterizedTest
    @MethodSource("paramsOfTestSubOptions")
    fun testSubOptions(argsStr: List<String>, expectedSubOptions: List<String>) {
        val args = MainArgs.of(argsStr)

        assertEquals(expectedSubOptions, args.subOptions)
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

        @JvmStatic
        fun paramsOfTestSubOptions(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), emptyList<String>()),
            arguments(listOf("--rep-suffix", "@"), emptyList<String>()),
            arguments(listOf("--rep-suffix=@"), emptyList<String>()),
            arguments(listOf("--version"), emptyList<String>()),
            arguments(listOf("grep"), emptyList<String>()),
            arguments(listOf("--rep-suffix", "@", "grep"), emptyList<String>()),
            arguments(listOf("--rep-suffix=@", "grep"), emptyList<String>()),
            arguments(listOf("grep", "--dummy"), listOf("--dummy")),
            arguments(listOf("--rep-suffix", "@", "grep", "--dummy"), listOf("--dummy")),
            arguments(listOf("--rep-suffix=@", "grep", "--dummy"), listOf("--dummy")),
            arguments(listOf("grep", "--dummy", "DUMMY"), listOf("--dummy", "DUMMY")),
            arguments(listOf("--rep-suffix", "@", "grep", "--dummy", "DUMMY"), listOf("--dummy", "DUMMY")),
            arguments(listOf("--rep-suffix=@", "grep", "--dummy", "DUMMY"), listOf("--dummy", "DUMMY")),
        )
    }
}
