package datasource.network.chronus.irkutsk.iguimit

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import library.logger.LogType
import library.logger.log
import model.chronus.EntryInfo
import model.chronus.Lesson
import model.chronus.Place
import model.chronus.Schedule
import model.chronus.ScheduleType
import model.chronus.addOrExtend
import model.chronus.asLessonType
import model.common.asLocalDateTime
import model.common.current
import model.common.isEven
import model.common.weekOfYear
import model.common.weekStartDay

suspend fun getLessons(client: HttpClient, json: Json, schedule: Schedule): List<Lesson>? {
	val response: String = try {
		when (schedule.type) {
			// example of schedule.url: https://raspmath.isu.ru/schedule/12
			ScheduleType.GROUP -> client.post("${Place.IRKUTSK_IGU_IMIT.defaultUrl}fillSchedule") {
				setBody("groupId=${schedule.url.substringAfterLast("/")}")
				contentType(ContentType.parse("application/x-www-form-urlencoded; charset=UTF-8"))
			}.body()
			// example of schedule.url: https://raspmath.isu.ru/searchPair?teacher=91
			ScheduleType.PERSON -> client.post("${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPairSTC") {
				setBody("subject_id=&teacher_id=${schedule.url.substringAfterLast("=")}&class_id=")
				contentType(ContentType.parse("application/x-www-form-urlencoded; charset=UTF-8"))
			}.body()
			// example of schedule.url: https://raspmath.isu.ru/searchPair?class=341
			ScheduleType.CLASSROOM -> client.post("${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPairSTC") {
				setBody("subject_id=&teacher_id=&class_id=${schedule.url.substringAfterLast("=")}")
				contentType(ContentType.parse("application/x-www-form-urlencoded; charset=UTF-8"))
			}.body()
			ScheduleType.OTHER -> throw IllegalStateException()
		}
	} catch (e: Exception) {
		log(LogType.NetworkClientError, e)
		return null
	}

	val currentWeekStart = LocalDate.current(Place.IRKUTSK_IGU_IMIT.city.timeZoneId).weekStartDay()
	val isCurrentWeekEven = currentWeekStart.weekOfYear().isEven()

	return try {
		val lessons = mutableListOf<Lesson>()
		val lessonDtos: List<LessonDto> = json.decodeFromString(response)

		lessonDtos.forEach {
			val date = currentWeekStart.plus(it.weekdayId - 1, DateTimeUnit.DAY)
			val startDate = LocalDate.parse(it.beginDatePairs)
			val endDate = LocalDate.parse(it.endDatePairs)

			val hours = when (it.timeId) {
				1 -> 8
				2 -> 10
				3 -> 11
				4 -> 13
				5 -> 15
				6 -> 17
				7 -> 18
				else -> throw IllegalStateException()
			}

			val minutes = when (it.timeId) {
				1, 5 -> 30
				2, 6 -> 10
				3, 4, 7 -> 50
				else -> throw IllegalStateException()
			}

			// subgroup is available for rare set of groups (e.g. `02123-ДБ`)
			val (name, subgroup) = if (it.subjectName.endsWith(" подгруппа)")) {
				val subjectNameWithoutPrefix = it.subjectName.removeSuffix(" подгруппа)")
				val realSubjectName = subjectNameWithoutPrefix.substringBeforeLast(" (")
				val subgroup = subjectNameWithoutPrefix.substringAfterLast(" (").toIntOrNull()
				if (subgroup != null) {
					realSubjectName to setOf(subgroup)
				} else { // on error, fallback to default implementation
					it.subjectName to emptySet()
				}
			} else {
				it.subjectName to emptySet()
			}

			val basicLesson = Lesson(
				name = name,
				startTime = date.asLocalDateTime(hours, minutes),
				durationInMinutes = Place.IRKUTSK_IGU_IMIT.lessonDurationInMinutes,
				type = it.typeSubjectName.asLessonType(),
				groups = setOf(
					EntryInfo(it.group ?: it.groupName ?: "", "${Place.IRKUTSK_IGU_IMIT.defaultUrl}schedule/${it.groupId}"),
				),
				subgroups = subgroup,
				persons = setOf(
					EntryInfo(it.teacherName, "${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPair?teacher=${it.teacherId}"),
				),
				classrooms = if (it.className != null) {
					setOf(
						EntryInfo(it.className, "${Place.IRKUTSK_IGU_IMIT.defaultUrl}searchPair?class=${it.classroomId}"),
					)
				} else {
					emptySet()
				},
				additionalInfo = "Ссылка на предмет: " +
					"${Place.IRKUTSK_IGU_IMIT.defaultUrl.substringAfter("https://")}searchPair?subject=${it.subjectId}",
			)

			if (it.week.isEmpty()) { // на чётных и нечётных неделях
				lessons.addIfInDateRange(
					lesson = basicLesson,
					startDate = startDate,
					endDate = endDate,
				)
				lessons.addIfInDateRange(
					lesson = basicLesson.copy(startTime = date.plus(1, DateTimeUnit.WEEK).asLocalDateTime(hours, minutes)),
					startDate = startDate,
					endDate = endDate,
				)
			} else { // на одной неделе
				val isLessonAtEvenWeek = it.week == "нижняя"
				if (isLessonAtEvenWeek == isCurrentWeekEven) {
					lessons.addIfInDateRange(
						lesson = basicLesson,
						startDate = startDate,
						endDate = endDate,
					)
				} else {
					lessons.addIfInDateRange(
						lesson = basicLesson.copy(startTime = date.plus(1, DateTimeUnit.WEEK).asLocalDateTime(hours, minutes)),
						startDate = startDate,
						endDate = endDate,
					)
				}
			}
		}

		lessons
	} catch (e: Exception) {
		log(LogType.ParseError, e)
		return null
	}
}

private fun MutableList<Lesson>.addIfInDateRange(lesson: Lesson, startDate: LocalDate, endDate: LocalDate) {
	val lessonDate = lesson.startTime.date
	if (startDate <= lessonDate && lessonDate <= endDate) {
		this.addOrExtend(lesson)
	}
}
