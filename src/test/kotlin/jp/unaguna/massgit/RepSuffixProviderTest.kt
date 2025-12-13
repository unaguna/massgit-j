package jp.unaguna.massgit

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class RepSuffixProviderTest {
    @ParameterizedTest
    @MethodSource("paramsOfTestRepSuffixProvider")
    fun testRepSuffixProvider(argsStr: List<String>, expectedRepSuffix: String) {
        val args = MainArgs.of(argsStr)
        val repSuffixProvider = RepSuffixProvider()

        val actualRepSuffix = repSuffixProvider.decideRefSuffix(args)

        assertEquals(expectedRepSuffix, actualRepSuffix)
    }

    companion object {
        @JvmStatic
        fun paramsOfTestRepSuffixProvider(): Stream<Arguments> = Stream.of(
            arguments(listOf("switch"), ": "),
            arguments(listOf("diff"), ": "),
            arguments(listOf("grep"), "/"),
            arguments(listOf("ls-files"), "/"),
        )
    }
}
