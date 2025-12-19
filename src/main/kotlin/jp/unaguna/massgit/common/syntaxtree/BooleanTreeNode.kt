package jp.unaguna.massgit.common.syntaxtree

sealed class BooleanTreeNode {
    var parent: BooleanTreeNode? = null

    /** 括弧による優先度。木の組み立て中にのみ使用する */
    var activePriority: Int = 0
    abstract val childCount: Int
    abstract fun getChildOrNull(index: Int): BooleanTreeNode?
    abstract fun popChildOrNull(index: Int): BooleanTreeNode?
    abstract val priority: Int
    abstract val vacancy: Int
    val isFull: Boolean get() = vacancy == 0
    val isEmpty: Boolean get() = vacancy == childCount
    val currentChildCount: Int get() = this.childCount - this.vacancy
    abstract fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean
    abstract fun appendChild(value: BooleanTreeNode)

    fun getChild(index: Int): BooleanTreeNode {
        return getChildOrNull(index)
            ?: throw IncompleteTreeException()
    }

    fun getLastChildOrNull(): BooleanTreeNode? {
        return if (this.isFull) {
            this.getChildOrNull(childCount - 1)!!
        } else {
            null
        }
    }

    fun popLastChildOrNull(): BooleanTreeNode? {
        return if (this.isFull) {
            this.popChildOrNull(childCount - 1)!!
        } else {
            null
        }
    }

    fun highPriorityThanOrEqual(other: BooleanTreeNode): Boolean {
        return when {
            this.activePriority < other.activePriority -> false
            this.activePriority > other.activePriority -> true
            this.priority < other.priority -> false
            this.priority > other.priority -> true
            else -> true
        }
    }

    fun sequenceOfChildren(): Sequence<BooleanTreeNode> = sequence {
        for (index in 0..<childCount) {
            val child = getChildOrNull(index)
            if (child != null) {
                yield(child)
            }
        }
    }

    fun isComplete(): Boolean {
        if (!this.isFull) { return false }
        return sequenceOfChildren().map { it.isComplete() }.all { it }
    }
}

class BooleanRootNode : BooleanTreeNode() {
    var child: BooleanTreeNode? = null
    override val priority: Int = Int.MIN_VALUE
    override val vacancy: Int get() = if (child == null) 1 else 0

    override fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean {
        return child?.evaluate(valueProvider)
            ?: throw IncompleteTreeException()
    }

    override val childCount: Int = 1
    override fun getChildOrNull(index: Int) = when (index) {
        0 -> child
        else -> throw ChildIndexOutOfBoundsException(index, size = childCount)
    }
    override fun popChildOrNull(index: Int): BooleanTreeNode? {
        val result = getChildOrNull(index)
        child = null
        return result
    }
    override fun appendChild(value: BooleanTreeNode) {
        if (child == null) {
            child = value
        } else {
            throw FullNodeException(childCount)
        }

        value.parent = this
    }
}

class BooleanVariableNodeImpl(override val name: String) : BooleanTreeNode(), BooleanVariableNode {
    override val childCount: Int = 0
    override fun getChildOrNull(index: Int): BooleanTreeNode? = null
    override fun popChildOrNull(index: Int): BooleanTreeNode? = null
    override val priority: Int = Int.MAX_VALUE
    override val vacancy: Int = 0
    override fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean = valueProvider.getValue(this)
    override fun appendChild(value: BooleanTreeNode) {
        throw FullNodeException(childCount)
    }
}

sealed class BooleanBinaryOperatorNode : BooleanTreeNode() {
    var left: BooleanTreeNode? = null
    var right: BooleanTreeNode? = null
    override val childCount: Int = 2
    override val vacancy: Int
        get() {
            var result = 0
            if (left == null) result += 1
            if (right == null) result += 1
            return result
        }

    override fun getChildOrNull(index: Int): BooleanTreeNode? = when (index) {
        0 -> left
        1 -> right
        else -> throw ChildIndexOutOfBoundsException(index, size = childCount)
    }

    override fun popChildOrNull(index: Int): BooleanTreeNode? = when (index) {
        0 -> {
            val result = left
            left = null
            result
        }
        1 -> {
            val result = right
            right = null
            result
        }
        else -> throw ChildIndexOutOfBoundsException(index, size = childCount)
    }

    override fun appendChild(value: BooleanTreeNode) {
        if (left == null) {
            left = value
        } else if (right == null) {
            right = value
        } else {
            throw FullNodeException(childCount)
        }

        value.parent = this
    }
}

class BooleanOrOperatorNode : BooleanBinaryOperatorNode() {
    override val priority: Int = 1
    override fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean {
        val left = left ?: throw IncompleteTreeException()
        val right = right ?: throw IncompleteTreeException()

        return left.evaluate(valueProvider) || right.evaluate(valueProvider)
    }
}

class BooleanAndOperatorNode : BooleanBinaryOperatorNode() {
    override val priority: Int = 2
    override fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean {
        val left = left ?: throw IncompleteTreeException()
        val right = right ?: throw IncompleteTreeException()

        return left.evaluate(valueProvider) && right.evaluate(valueProvider)
    }
}
