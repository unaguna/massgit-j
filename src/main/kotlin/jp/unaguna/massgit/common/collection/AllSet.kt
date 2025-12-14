package jp.unaguna.massgit.common.collection

class AllSet<E> : Set<E> {
    override val size: Int
        get() = error("Not Implemented")
    override fun isEmpty(): Boolean = false
    override fun contains(element: E): Boolean = true
    override fun iterator(): Iterator<E> = error("Not Implemented")
    override fun containsAll(elements: Collection<E>): Boolean = true
}
