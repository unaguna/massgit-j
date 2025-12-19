package jp.unaguna.massgit.common.syntaxtree

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BooleanTreeDecodeTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "var1",
            "abc",
        ]
    )
    fun `decode variable`(variableName: String) {
        val root = BooleanTreeImpl.decodeToNode(variableName)

        assertIs<BooleanVariableNode>(root)
        assertEquals(root.name, variableName)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 and var2",
            "var1 and  var2",
            "var1  and var2",
        ]
    )
    fun `decode and operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanAndOperatorNode>(root)
        val left = assertIs<BooleanVariableNode>(root.left)
        val right = assertIs<BooleanVariableNode>(root.right)
        assertEquals(left.name, "var1")
        assertEquals(right.name, "var2")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 or var2",
            "var1 or  var2",
            "var1  or var2",
        ]
    )
    fun `decode or operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanOrOperatorNode>(root)
        val left = assertIs<BooleanVariableNode>(root.left)
        val right = assertIs<BooleanVariableNode>(root.right)
        assertEquals(left.name, "var1")
        assertEquals(right.name, "var2")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 and var2 and var3",
            "( var1 and var2 ) and var3",
        ]
    )
    fun `decode 'and' 'and' operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanAndOperatorNode>(root)
        val left = assertIs<BooleanAndOperatorNode>(root.left)
        val right = assertIs<BooleanVariableNode>(root.right)
        val leftLeft = assertIs<BooleanVariableNode>(left.left)
        val leftRight = assertIs<BooleanVariableNode>(left.right)
        assertEquals(leftLeft.name, "var1")
        assertEquals(leftRight.name, "var2")
        assertEquals(right.name, "var3")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 or var2 or var3",
            "( var1 or var2 ) or var3",
        ]
    )
    fun `decode 'or' 'or' operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanOrOperatorNode>(root)
        val left = assertIs<BooleanOrOperatorNode>(root.left)
        val right = assertIs<BooleanVariableNode>(root.right)
        val leftLeft = assertIs<BooleanVariableNode>(left.left)
        val leftRight = assertIs<BooleanVariableNode>(left.right)
        assertEquals(leftLeft.name, "var1")
        assertEquals(leftRight.name, "var2")
        assertEquals(right.name, "var3")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 or var2 and var3",
            "var1 or ( var2 and var3 )",
        ]
    )
    fun `decode 'or' 'and' operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanOrOperatorNode>(root)
        val left = assertIs<BooleanVariableNode>(root.left)
        val right = assertIs<BooleanAndOperatorNode>(root.right)
        assertEquals(left.name, "var1")
        val rightLeft = assertIs<BooleanVariableNode>(right.left)
        val rightRight = assertIs<BooleanVariableNode>(right.right)
        assertEquals(rightLeft.name, "var2")
        assertEquals(rightRight.name, "var3")
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "var1 and var2 or var3",
            "( var1 and var2 ) or var3",
        ]
    )
    fun `decode 'and' 'or' operator`(expression: String) {
        val root = BooleanTreeImpl.decodeToNode(expression)

        assertIs<BooleanOrOperatorNode>(root)
        val left = assertIs<BooleanAndOperatorNode>(root.left)
        val right = assertIs<BooleanVariableNode>(root.right)
        val leftLeft = assertIs<BooleanVariableNode>(left.left)
        val leftRight = assertIs<BooleanVariableNode>(left.right)
        assertEquals(leftLeft.name, "var1")
        assertEquals(leftRight.name, "var2")
        assertEquals(right.name, "var3")
    }
}
