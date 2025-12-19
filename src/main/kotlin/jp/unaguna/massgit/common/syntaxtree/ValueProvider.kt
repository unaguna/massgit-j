package jp.unaguna.massgit.common.syntaxtree

interface ValueProvider<T> {
    fun getValue(variable: BooleanVariableNode): T

    companion object {
        fun fromTrueSet(trueVariableNames: Set<String>): ValueProvider<Boolean> {
            return BooleanValueProvider(trueVariableNames)
        }
    }
}

private class BooleanValueProvider(private val trueVariableNames: Set<String>) : ValueProvider<Boolean> {
    override fun getValue(variable: BooleanVariableNode): Boolean {
        return trueVariableNames.contains(variable.name)
    }
}
