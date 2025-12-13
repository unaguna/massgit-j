package jp.unaguna.massgit

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

class MassgitOptionsTest {
    @ParameterizedTest
    @MethodSource("paramsOfTestRepSuffix")
    fun testRepSuffix(argsStr: List<String>, expectedRepSuffix: String?) {
        val (massgitOptions, _) = MassgitOptions.build(argsStr)
        assertEquals(expectedRepSuffix, massgitOptions.getRepSuffix())
    }

    companion object {
        @JvmStatic
        fun paramsOfTestRepSuffix(): Stream<Arguments> = Stream.of(
            arguments(emptyList<String>(), null),
            arguments(listOf("--rep-suffix", "@"), "@"),
            arguments(listOf("--rep-suffix=@"), "@"),
            arguments(listOf("--version"), null),
        )
    }
}
