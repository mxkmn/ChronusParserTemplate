package datasource.network.chronus.irkutsk.irnitu

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import library.logger.LogType
import library.logger.log
import model.chronus.Place
import model.chronus.Schedule
import model.chronus.ScheduleType

suspend fun getSearchResults(client: HttpClient, query: String): List<Schedule>? {
	val page: String = try {
		client.get("${Place.IRKUTSK_IRNITU.defaultUrl}?search=$query").body()
	} catch (e: Exception) {
		log(LogType.NetworkClientError, e)
		return null
	}

	return try {
		withContext(Dispatchers.Default) {
			val document = Ksoup.parse(page)
			val classWithResults = document.getElementsByClass("content ").firstOrNull()
				?: return@withContext emptyList()

			classWithResults.getElementsByTag("li")
				.mapNotNull { if (it.childrenSize() > 0) it.child(0) else null }
				.map { a ->
					val url = Place.IRKUTSK_IRNITU.defaultUrl + a.attr("href")
					val name = a.text()
						.replace("<b>", "").replace("</b>", "") // website bug fix
					val type = when {
						url.contains("group") -> ScheduleType.GROUP
						url.contains("prep") -> ScheduleType.PERSON
						url.contains("aud") -> ScheduleType.CLASSROOM
						else -> ScheduleType.OTHER // never happens
					}
					Schedule(name, type, Place.IRKUTSK_IRNITU, url)
				}
		}
	} catch (e: Exception) {
		log(LogType.ParseError, e)
		null
	}
}
