package jp.unaguna.massgit.common.collection

class Either<A, B> private constructor(val isLeft: Boolean, private val left: A?, private val right: B?) {
    val isRight = !isLeft

    fun getLeft(): A {
        check(isLeft) { "the Either object is not left" }
        return left as A
    }

    fun getRight(): B {
        check(isRight) { "the Either object is not right" }
        return right as B
    }

    fun isLeftAnd(condition: (A) -> Boolean): Boolean {
        return isLeft && condition(getLeft())
    }

    fun isRightAnd(condition: (B) -> Boolean): Boolean {
        return isRight && condition(getRight())
    }

    companion object {
        fun <A, B> left(left: A) = Either(true, left, null as B?)
        fun <A, B> right(right: B) = Either(false, null as A?, right)
    }
}

fun <T> Result<T>.getEither(): Either<T, Throwable> {
    return this.fold(
        { Either.left(it) },
        { Either.right(it) },
    )
}

fun <A, B> List<Either<A, B>>.groupByType(): Pair<List<A>, List<B>> {
    val resultA = mutableListOf<A>()
    val resultB = mutableListOf<B>()
    this.forEach {
        if (it.isLeft) {
            resultA.add(it.getLeft())
        } else {
            resultB.add(it.getRight())
        }
    }
    return Pair(resultA, resultB)
}
