package jp.unaguna.massgit

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class MassgitOptionsTest {
    @ParameterizedTest
    @MethodSource("paramsOfTestRemainingArgs")
    fun testRemainingArgs(argsStr: List<String>, expectedRemainingArgs: List<String>) {
        val (_, actualRemainingArgs) = MassgitOptions.build(argsStr)
        assertEquals(expectedRemainingArgs, actualRemainingArgs)
    }

    @ParameterizedTest
    @MethodSource("paramsOfTestRepSuffix")
    fun testRepSuffix(argsStr: List<String>, expectedRepSuffix: String?) {
        val (massgitOptions, _) = MassgitOptions.build(argsStr)
        assertEquals(expectedRepSuffix, massgitOptions.getRepSuffix())
    }

    @ParameterizedTest
    @MethodSource("paramsOfTestVersion")
    fun testVersion(argsStr: List<String>, expectedVersion: Boolean) {
        val (massgitOptions, _) = MassgitOptions.build(argsStr)
        assertEquals(expectedVersion, massgitOptions.isVersion())
    }

    companion object {
        @JvmStatic
        fun paramsOfTestRemainingArgs(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), emptyList<String>()),
            arguments(listOf("--rep-suffix", "@"), emptyList<String>()),
            arguments(listOf("--rep-suffix=@"), emptyList<String>()),
            arguments(listOf("--rep-suffix", "@", "grep"), listOf("grep")),
            arguments(listOf("--rep-suffix=@", "grep"), listOf("grep")),
            arguments(listOf("--rep-suffix", "@", "grep", "--dummy"), listOf("grep", "--dummy")),
            arguments(listOf("--rep-suffix=@", "grep", "--dummy"), listOf("grep", "--dummy")),
            arguments(listOf("--version"), emptyList<String>()),
        )

        @JvmStatic
        fun paramsOfTestRepSuffix(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), null),
            arguments(listOf("--rep-suffix", "@"), "@"),
            arguments(listOf("--rep-suffix=@"), "@"),
            arguments(listOf("--rep-suffix= "), " "),
            arguments(listOf("--rep-suffix="), ""),
            arguments(listOf("--version"), null),
        )

        @JvmStatic
        fun paramsOfTestVersion(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), false),
            arguments(listOf("--rep-suffix", "@"), false),
            arguments(listOf("--version"), true),
            arguments(listOf("--version", "--rep-suffix", "@"), true),
            arguments(listOf("--rep-suffix", "@", "--version"), true),
        )
    }
}
