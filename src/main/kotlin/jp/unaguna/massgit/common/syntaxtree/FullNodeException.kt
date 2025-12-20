package jp.unaguna.massgit.common.syntaxtree

class FullNodeException(size: Int) : Exception("the node has $size children and is full")
