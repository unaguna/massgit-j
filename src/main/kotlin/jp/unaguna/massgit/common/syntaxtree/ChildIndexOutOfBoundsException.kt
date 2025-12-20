package jp.unaguna.massgit.common.syntaxtree

class ChildIndexOutOfBoundsException(index: Int, size: Int) :
    IndexOutOfBoundsException("Child index $index is out of range [0, $size).")
