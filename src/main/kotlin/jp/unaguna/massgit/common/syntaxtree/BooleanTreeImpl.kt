package jp.unaguna.massgit.common.syntaxtree

class BooleanTreeImpl private constructor(expression: String) : BooleanTree {
    private val node = decodeToNode(expression)

    override fun evaluate(valueProvider: ValueProvider<Boolean>): Boolean = node.evaluate(valueProvider)

    companion object {
        fun decode(expression: String): BooleanTree = BooleanTreeImpl(expression)

        fun decodeToNode(exception: String): BooleanTreeNode {
            val root = BooleanRootNode()

            // 現在の優先度。計算優先度を変更するかっこのネストの数
            var currentPriority = 0
            // 最後に木に追加したノード
            var lastNode: BooleanTreeNode = root
            val tokens = splitTokens(exception)
            for (token in tokens) {
                // トークンが括弧であれば、優先度を加減するだけでトークン処理を完了する
                tokenToPriority(token)?.let { deltaPriority ->
                    currentPriority += deltaPriority
                    continue
                }

                val node = tokenToNode(token).apply {
                    activePriority = currentPriority
                }

                appendChildWithPriority(lastNode, node)
                lastNode = node
            }

            val result = root.child ?: error("failed to decode $exception")
            require(currentPriority == 0) {
                "There is no closing bracket corresponding to the opening bracket."
            }
            check(result.isComplete()) {
                throw IncompleteTreeException("failed to decode $exception")
            }
            return result
        }

        private fun splitTokens(expression: String): List<String> {
            val tokens = mutableListOf<String>()

            // expression をトークンに分割する。
            // TODO: 文字種が変わる個所もトークンの区切りとして扱う
            expression.split(Regex("\\s+")).forEach { tokens.add(it.trim()) }

            return tokens
        }

        private fun tokenToPriority(token: String): Int? {
            return when (token) {
                "(" -> 1
                ")" -> -1
                else -> null
            }
        }

        private fun tokenToNode(token: String): BooleanTreeNode {
            return when (token) {
                "or" -> BooleanOrOperatorNode()
                "and" -> BooleanAndOperatorNode()
                "not" -> BooleanNotOperatorNode()
                else -> BooleanVariableNodeImpl(token)
            }
        }

        private fun appendChildWithPriority(parent: BooleanTreeNode, child: BooleanTreeNode) {
            // 木の構成上正常なのは、以下の2パターンのみ
            // 1. 空きが1つのノードに、子の上限が1以下のノードを追加する
            // 2. 空きが無いノードに、子の上限が2かつ空のノードを追加する

            if (parent.vacancy == 1 && child.childCount <= 1) {
                parent.appendChild(child)
            } else if (parent.isFull && child.childCount == 2 && child.isEmpty) {
                if (parent.highPriorityThanOrEqual(child)) {
                    appendChildWithPriority(parent.parent!!, child)
                } else {
                    val replacedChild = parent.popLastChildOrNull()
                        ?: error("parent must have a child and be full; parent=$parent, child=$child")
                    child.appendChild(replacedChild)
                    parent.appendChild(child)
                }
            } else {
                error(
                    "failed to append $child into $parent; " +
                        "parent.vacancy=${parent.vacancy}, child.childCount=${child.childCount}"
                )
            }
        }
    }
}
