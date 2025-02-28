package datasource.network.chronus.irkutsk.iguimit

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import library.logger.LogType
import library.logger.log
import model.chronus.Place
import model.chronus.Schedule
import model.chronus.ScheduleType

suspend fun getSearchResults(client: HttpClient): List<Schedule>? = coroutineScope {
	val groups = async {
		val pageGroups: String = try {
			client.get("${Place.IRKUTSK_IGU_IMIT.defaultUrl}selectGroup").body()
		} catch (e: Exception) {
			log(LogType.NetworkClientError, e)
			return@async null
		}

		withContext(Dispatchers.Default) {
			parseGroupsHtml(pageGroups)
		}
	}

	val personsAndClassrooms = async {
		val pagePersonsAndClassrooms: String = try {
			client.get("${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPair").body()
		} catch (e: Exception) {
			log(LogType.NetworkClientError, e)
			return@async null
		}

		withContext(Dispatchers.Default) {
			parsePersonsAndClassroomsHtml(pagePersonsAndClassrooms)
		}
	}

	listOf(groups, personsAndClassrooms).awaitAll()
		.flatMap { it ?: return@coroutineScope null }
}

private fun parseGroupsHtml(page: String): List<Schedule>? = try {
	val doc = Ksoup.parse(page)

	doc.select("div.selectGroup-course-col").flatMap { course ->
		course.select("li.item-schedule a").map { group ->
			Schedule(
				name = group.text(),
				type = ScheduleType.GROUP,
				place = Place.IRKUTSK_IGU_IMIT,
				url = Place.IRKUTSK_IGU_IMIT.defaultUrl + group.attr("href").removePrefix("/"),
			)
		}
	}
} catch (e: Exception) {
	log(LogType.ParseError, e)
	null
}

private fun parsePersonsAndClassroomsHtml(page: String): List<Schedule>? {
	try {
		val doc = Ksoup.parse(page)

		val persons = doc.select("select#searchTeacher option").mapNotNull { element ->
			getPersonOrClassroomSchedule(element, ScheduleType.PERSON)
		}

		val classrooms = doc.select("select#searchClass option").mapNotNull { element ->
			getPersonOrClassroomSchedule(element, ScheduleType.CLASSROOM)
		}

		return persons + classrooms
	} catch (e: Exception) {
		log(LogType.ParseError, e)
		return null
	}
}

private fun getPersonOrClassroomSchedule(element: Element, scheduleType: ScheduleType): Schedule? {
	val text = element.text()
	val value = element.attr("value")
	return if (value.isNotEmpty() && text.isNotEmpty()) {
		val url = "${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPair?" + when (scheduleType) {
			ScheduleType.PERSON -> "teacher="
			ScheduleType.CLASSROOM -> "class="
			else -> throw IllegalStateException()
		} + value

		Schedule(
			name = text,
			type = scheduleType,
			place = Place.IRKUTSK_IGU_IMIT,
			url = url,
		)
	} else {
		null
	}
}
