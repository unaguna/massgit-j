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
    fun testSubCommand(argsStr: List<String>, expectedSubCommand: Subcommand?) {
        val args = MainArgs.of(argsStr)

        assertEquals(expectedSubCommand, args.subCommand)
    }

    @ParameterizedTest
    @MethodSource("paramsOfTestSubCommandObject")
    fun testSubCommandObject(argsStr: List<String>, expectedSubCommand: Subcommand?) {
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
            arguments(listOf("grep"), Subcommand.of("grep")),
            arguments(listOf("--rep-suffix", "@", "grep"), Subcommand.of("grep")),
            arguments(listOf("--rep-suffix=@", "grep"), Subcommand.of("grep")),
            arguments(listOf("grep", "--dummy"), Subcommand.of("grep")),
            arguments(listOf("--rep-suffix", "@", "grep", "--dummy"), Subcommand.of("grep")),
            arguments(listOf("--rep-suffix=@", "grep", "--dummy"), Subcommand.of("grep")),
        )

        @JvmStatic
        fun paramsOfTestSubCommandObject(): Stream<Arguments> = Stream.of(
            arguments(listOf("grep"), Subcommand.Grep),
            arguments(listOf("diff"), Subcommand.Diff),
            arguments(listOf("ls-files"), Subcommand.LsFiles),
            arguments(listOf("fetch"), Subcommand.OtherGitSubcommand("fetch")),
            arguments(listOf("pull"), Subcommand.OtherGitSubcommand("pull")),
            arguments(listOf("push"), Subcommand.OtherGitSubcommand("push")),
            arguments(listOf("switch"), Subcommand.OtherGitSubcommand("switch")),
            arguments(listOf("reset"), Subcommand.OtherGitSubcommand("reset")),
            arguments(listOf("checkout"), Subcommand.OtherGitSubcommand("checkout")),
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
