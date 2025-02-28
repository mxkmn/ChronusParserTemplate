package datasource.network.chronus.irkutsk.irnitu

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import library.logger.LogType
import library.logger.log
import model.chronus.EntryInfo
import model.chronus.Lesson
import model.chronus.Place
import model.chronus.Schedule
import model.chronus.asLessonType
import model.common.asDayOfWeek
import model.common.asLocalDateTime
import model.common.asyncMap
import model.common.current
import model.common.parseRus
import model.common.trimAndCapitalizeFirstChar
import kotlin.text.split

suspend fun getLessons(client: HttpClient, schedule: Schedule): List<Lesson>? {
	return (-2..2).toList().asyncMap { week -> // take the previous two, the current and the next two weeks
		val date = LocalDate.current().plus(week, DateTimeUnit.WEEK)
		// example of schedule.url: https://www.istu.edu/schedule/?group=470511
		val url = "${schedule.url}&date=${date.year}-${date.month.ordinal + 1}-${date.dayOfMonth}"

		val page: String = try {
			log(LogType.NetworkRequestUrl, "Trying to get data from $url") // additional data for customized URLs
			client.get(url).body()
		} catch (e: Exception) {
			log(LogType.NetworkClientError, e)
			return@asyncMap null
		}

		withContext(Dispatchers.Default) { // at maximum CPU speed
			parseScheduleHtml(page)?.also { // parse data from HTML and also, after parsing
				if (it.isEmpty()) { // if the schedule is empty (no lessons found on the page)
					log(LogType.CornerCase, "No schedule info at $url") // logging this just in case
				}
			}
		}
	}.flatMap { it ?: return null } // if an error occurred during data request, terminate the execution
}

private fun parseScheduleHtml(page: String): List<Lesson>? = try {
	val doc = Ksoup.parse(page)

	val startDate = doc.getElementsByClass("alert alert-info")
		.select("p:containsOwn(действия:)").first()?.let { // select the date description row
			it.text().removePrefix("время действия: ") // select an interval
				.substringBefore(' ') // select only the start date
				.let { LocalDate.parseRus(it) }
		}

	if (startDate != null) {
		doc.getElementsByClass("full-odd-week").firstOrNull()?.let {
			return parseWeekContainer(it, startDate, isEven = false)
		}

		doc.getElementsByClass("full-even-week").firstOrNull()?.let {
			return parseWeekContainer(it, startDate, isEven = true)
		}
	}

	emptyList()
} catch (e: Exception) {
	log(LogType.ParseError, e)
	null
}

private fun parseWeekContainer(
	weekContainer: Element,
	startDate: LocalDate,
	isEven: Boolean, // is it an even week?
): List<Lesson> {
	val permittedLessonTypesForWeek = listOf(
		"class-tail class-all-week",
		if (isEven) "class-tail class-even-week" else "class-tail class-odd-week",
	)

	val lessons = mutableListOf<Lesson>()
	var currentDate: LocalDate? = null

	weekContainer.children().forEach { dayItem ->
		if (dayItem.className() == "day-heading") { // date header
			currentDate = dayItem.text().substringBefore(',').asDayOfWeek()
				?.let { startDate.plus(it.ordinal, DateTimeUnit.DAY) }
		} else if (dayItem.className() == "class-lines") { // day container with date and pairs
			dayItem.children().map { it.child(0).children() } // selecting time and lessons from every day
				.forEach { timeAndLessonsContainer ->
					addLessonsFromContainer(
						timeAndLessonsContainer = timeAndLessonsContainer,
						permittedLessonTypesForWeek = permittedLessonTypesForWeek,
						currentDate = currentDate ?: return@forEach,
						lessons = lessons,
					)
				}
		}
	}

	return lessons
}

private fun addLessonsFromContainer(
	timeAndLessonsContainer: Elements,
	permittedLessonTypesForWeek: List<String>,
	currentDate: LocalDate,
	lessons: MutableList<Lesson>,
) {
	var time: LocalDateTime? = null

	timeAndLessonsContainer.forEach { timeOrLesson ->
		val containerType = timeOrLesson.className()
		if (containerType == "class-time") { // if the card provides a time for next lessons
			val (hour, minute) = timeOrLesson.text().split(":").map { it.toIntOrNull() }
				.takeIf { it.size == 2 && !it.contains(null) }?.filterNotNull() ?: return@forEach

			time = currentDate.asLocalDateTime(hour, minute)
		} else if (time != null && containerType in permittedLessonTypesForWeek) { // if the card shows a lesson info
			var name: String? = null
			var type: String? = null
			val groups = mutableSetOf<EntryInfo>()
			val subgroups = mutableSetOf<Int>()
			val persons = mutableSetOf<EntryInfo>()
			val classrooms = mutableSetOf<EntryInfo>()

			timeOrLesson.children().forEach { info ->
				when (info.className()) {
					"class-pred" -> {
						name = info.text().trimAndCapitalizeFirstChar()
					}
					"class-aud" -> {
						info.children().takeIf { it.isNotEmpty() }?.forEach { child ->
							classrooms += getClassroom(child)
						} ?: run {
							classrooms += getClassroom(info)
						}
					}
					"class-info" -> {
						val contentInTag = info.html()
						if (info.children().isEmpty() || !info.child(0).attributes().toString().contains("group")) {
							type = contentInTag.substringBefore("<a").trimAndCapitalizeFirstChar()

							info.children().forEach {
								persons.add(EntryInfo(it.ownText(), Place.IRKUTSK_IRNITU.defaultUrl + it.attr("href")))
							}
						} else {
							info.children().forEach { child ->
								child.attr("href").takeIf { it.contains("group") }?.let { relativeUrl ->
									groups.add(EntryInfo(child.ownText(), Place.IRKUTSK_IRNITU.defaultUrl + relativeUrl))
								}
							}

							contentInTag.let { if (it.contains("</a>")) it.substringAfterLast("</a>") else it }.trim()
								.takeIf { it.contains("подгруппа ") }?.substringAfter("подгруппа ")?.toIntOrNull()?.let {
									subgroups.add(it)
								}
						}
					}
				}
			}

			if (name?.startsWith("Проект | ") == true) {
				name = name!!.substringAfter("Проект | ")
				type = "Проект"
			}

			name?.takeIf { it.isNotBlank() }?.let { name ->
				lessons.add(
					Lesson(
						name = name,
						startTime = time!!,
						durationInMinutes = Place.IRKUTSK_IRNITU.lessonDurationInMinutes,
						type = type.asLessonType(),
						groups = groups,
						subgroups = subgroups,
						persons = persons,
						classrooms = classrooms,
					),
				)
			}
		}
	}
}

private fun getClassroom(classroomInfo: Element): List<EntryInfo> {
	val classroomName = classroomInfo.ownText().takeIf { it.isNotBlank() }
	val classroomUrl = classroomInfo.attr("href").takeIf { it.isNotBlank() }?.let { Place.IRKUTSK_IRNITU.defaultUrl + it }

	return if (classroomName.isNullOrBlank() || classroomName == "-") {
		emptyList()
	} else if (classroomUrl != null) {
		listOf(EntryInfo(classroomName, classroomUrl))
	} else {
		classroomName.split(" ").filter { it != "/" }.map { EntryInfo(it, null) }
	}
}
