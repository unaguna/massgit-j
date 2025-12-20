package jp.unaguna.massgit.common.syntaxtree

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class BooleanValueProviderTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "var1,true",
            "var2,true",
            "abc,false",
        ]
    )
    fun testValueOfBooleanVariable(variableName: String, expected: Boolean) {
        val provider = ValueProvider.fromTrueSet(setOf("var1", "var2"))
        val variableNode = BooleanVariableNodeImpl(variableName)

        assertEquals(expected, provider.getValue(variableNode))
    }
}
