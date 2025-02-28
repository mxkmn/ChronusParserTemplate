package datasource.network.chronus.main

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import library.logger.LogType
import library.logger.log
import model.chronus.Lesson
import model.chronus.Place
import model.chronus.Schedule

object ChronusNetwork {
	// FIXME: прикрутить Manual DI (https://mishkun.xyz/blog/Manual-DI-Cookbook.html) вместо глобального объекта
	private val json: Json = defaultJson()
	private val client: HttpClient = defaultHttpClient(json)

	private val lessonsCache = NetworkCache<Set<Lesson>>()
	private val searchResultsCache = NetworkCache<List<Schedule>>()

	suspend fun getLessons(schedule: Schedule): Set<Lesson>? {
		log(LogType.NetworkRequestUrl, "Trying to getLessons for $schedule")
		lessonsCache[schedule.place, schedule.url]?.let { return it }

		val lessons = when (schedule.place) {
			Place.IRKUTSK_IRNITU -> datasource.network.chronus.irkutsk.irnitu.getLessons(client, schedule)
			Place.IRKUTSK_IGU_IMIT -> datasource.network.chronus.irkutsk.iguimit.getLessons(client, json, schedule)
			Place.YOUR_PLACE -> datasource.network.chronus.yourcity.youruniversity.getLessons(client, json, schedule)
		}?.toSet()

		if (lessons != null) {
			lessonsCache[schedule.place, schedule.url] = lessons
		}

		return lessons
	}

	suspend fun getSearchResults(
		place: Place,
		query: String, // name of group, teacher or classroom. Empty ("") if Place.minSearchChars == null
	): List<Schedule>? = try {
		log(LogType.NetworkRequestUrl, "Trying to getSearchResults: place=$place, query=$query")
		searchResultsCache[place, query]?.let {
			log(LogType.NetworkRequestUrl, "Found result in cache: place=$place, query=$query")
			return it
		}

		val result = when (place) {
			Place.IRKUTSK_IRNITU -> datasource.network.chronus.irkutsk.irnitu.getSearchResults(client, query)
			Place.IRKUTSK_IGU_IMIT -> datasource.network.chronus.irkutsk.iguimit.getSearchResults(client)
			Place.YOUR_PLACE -> datasource.network.chronus.yourcity.youruniversity.getSearchResults(client, json, query)
		}?.also { schedules ->
			if (schedules.isEmpty()) {
				log(LogType.CornerCase, "No schedules found: place=$place, query=$query")
			} else {
				searchResultsCache[place, query] = schedules
			}
		}

		result
	} catch (e: Exception) {
		log(LogType.IOError, "place=$place, query=$query", e)
		null
	}
}
