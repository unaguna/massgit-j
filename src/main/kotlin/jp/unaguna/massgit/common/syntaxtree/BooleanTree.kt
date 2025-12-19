package jp.unaguna.massgit.common.syntaxtree

interface BooleanTree {
    fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean
}
