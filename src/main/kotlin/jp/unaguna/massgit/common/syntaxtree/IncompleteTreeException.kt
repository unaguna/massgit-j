package jp.unaguna.massgit.common.syntaxtree

class IncompleteTreeException(msg: String? = null) : Exception(buildMsg(msg)) {
    companion object {
        private fun buildMsg(msg: String?): String {
            return when (msg) {
                null -> "the boolean expression tree is incomplete."
                else -> "$msg: the boolean expression tree is incomplete."
            }
        }
    }
}
