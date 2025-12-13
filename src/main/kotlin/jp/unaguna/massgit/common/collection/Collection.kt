package jp.unaguna.massgit.common.collection

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

fun <E, R> List<E>.submitForEach(executor: ExecutorService, task: (e: E) -> R): List<Future<R>> {
    return this.map {
        executor.submit<R> { task(it) }
    }
}

fun <E> Collection<E>.containsAny(vararg value: E): Boolean {
    return value.any { contains(it) }
}
