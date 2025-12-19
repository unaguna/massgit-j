package jp.unaguna.massgit.common.syntaxtree

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class BooleanTreeEvaluateTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "var1,true",
            "abc,false",
        ]
    )
    fun `evaluate variable`(variableName: String, expectedValue: Boolean) {
        val valueProvider = ValueProvider.fromTrueSet(setOf("var1", "var2"))
        val tree = BooleanTreeImpl.decode(variableName)

        assertEquals(expectedValue, tree.evaluate(valueProvider))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 or var2,true",
            "var1 or abc,true",
            "abc or var2,true",
            "abc or def,false",
        ]
    )
    fun `evaluate 'or' operator`(variableName: String, expectedValue: Boolean) {
        val valueProvider = ValueProvider.fromTrueSet(setOf("var1", "var2"))
        val tree = BooleanTreeImpl.decode(variableName)

        assertEquals(expectedValue, tree.evaluate(valueProvider))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 and var2,true",
            "var1 and abc,false",
            "abc and var2,false",
            "abc and def,false",
        ]
    )
    fun `evaluate 'and' operator`(variableName: String, expectedValue: Boolean) {
        val valueProvider = ValueProvider.fromTrueSet(setOf("var1", "var2"))
        val tree = BooleanTreeImpl.decode(variableName)

        assertEquals(expectedValue, tree.evaluate(valueProvider))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 and var2 and var3,true",
            "abc and var2 and var3,false",
            "var1 and abc and var3,false",
            "var1 and var2 and abc,false",
            "var1 or abc or def,true",
            "abc or var2 or def,true",
            "abc or def or var3,true",
            "abc or def or ghi,false",
            "var1 and var2 or var3 and abc,true",
            "var1 or abc and def or abc,true",
            "abc or def and def or var3,true",
        ]
    )
    fun `evaluate 2 operator`(variableName: String, expectedValue: Boolean) {
        val valueProvider = ValueProvider.fromTrueSet(setOf("var1", "var2", "var3"))
        val tree = BooleanTreeImpl.decode(variableName)

        assertEquals(expectedValue, tree.evaluate(valueProvider))
    }
}
