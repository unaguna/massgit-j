package jp.unaguna.massgit.common.collection

import java.io.Closeable

class ClosablePair<A : Closeable, B : Closeable> private constructor(
    val left: A,
    val right: B,
) : Closeable {
    operator fun component1() = left
    operator fun component2() = right

    override fun close() {
        val throwableA = runCatching {
            left.close()
        }.exceptionOrNull()
        val throwableB = runCatching {
            right.close()
        }.exceptionOrNull()

        if (throwableA != null) {
            throw throwableA
        }
        if (throwableB != null) {
            throw throwableB
        }
    }

    companion object {
        fun <A : Closeable, B : Closeable> of(constructA: () -> A, constructB: () -> B): ClosablePair<A, B> {
            var a: A? = null
            var b: B? = null

            runCatching {
                a = constructA()
                b = constructB()
            }.onFailure {
                a?.close()
                b?.close()
            }.exceptionOrNull()?.let { throw it }

            return ClosablePair(a!!, b!!)
        }
    }
}
