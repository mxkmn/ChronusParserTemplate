package model.common

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> Iterable<T>.asyncMap(
	transform: suspend (T) -> R,
): List<R> = coroutineScope {
	this@asyncMap.map { item ->
		async {
			transform(item)
		}
	}.awaitAll()
}
