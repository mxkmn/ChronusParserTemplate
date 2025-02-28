package datasource.network.chronus.main

import kotlinx.datetime.Clock
import model.chronus.Place

class NetworkCache<T : Collection<Any>> {
	private val cache = mutableMapOf<PlaceAndQuery, CollectionAndSyncTime<T>>()

	operator fun get(place: Place, url: String): T? =
		cache[PlaceAndQuery(place, url)]?.takeIf { it.cachedAt + 15 * 60 > Clock.System.now().epochSeconds }?.collection

	operator fun set(place: Place, url: String, collection: T) {
		cache[PlaceAndQuery(place, url)] = CollectionAndSyncTime(collection, Clock.System.now().epochSeconds)
	}

	fun getWithTimeIgnorance(place: Place, url: String): T? =
		cache[PlaceAndQuery(place, url)]?.collection
}

private data class PlaceAndQuery(
	val place: Place,
	val query: String,
)

private data class CollectionAndSyncTime<T>(
	val collection: T,
	val cachedAt: Long,
)
