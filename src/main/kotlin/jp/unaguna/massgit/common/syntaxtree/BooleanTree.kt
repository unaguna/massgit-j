package jp.unaguna.massgit.common.syntaxtree

interface BooleanTree {
    fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean

    companion object {
        fun decode(expression: String): BooleanTree {
            return BooleanTreeImpl.decode(expression)
        }
    }
}
