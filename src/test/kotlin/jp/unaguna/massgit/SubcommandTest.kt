package jp.unaguna.massgit

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals

class SubcommandTest {
    @Test
    fun testSubCommandEqually() {
        assertEquals(Subcommand.OtherGitSubcommand("fetch"), Subcommand.OtherGitSubcommand("fetch"))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "diff",
            "grep",
            "ls-files",
        ]
    )
    fun testOtherGitSubcommandConstructionError(subcommand: String) {
        assertThrows<IllegalStateException> { Subcommand.OtherGitSubcommand(subcommand) }
    }
}
